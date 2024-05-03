package org.kunlab.scenamatica.context.stage.nms.v1_16_R3;

import com.mojang.serialization.Lifecycle;
import net.minecraft.server.v1_16_R3.BiomeManager;
import net.minecraft.server.v1_16_R3.ChunkGenerator;
import net.minecraft.server.v1_16_R3.Convertable;
import net.minecraft.server.v1_16_R3.DimensionManager;
import net.minecraft.server.v1_16_R3.DynamicOpsNBT;
import net.minecraft.server.v1_16_R3.EnumDifficulty;
import net.minecraft.server.v1_16_R3.EnumGamemode;
import net.minecraft.server.v1_16_R3.GameRules;
import net.minecraft.server.v1_16_R3.GeneratorSettings;
import net.minecraft.server.v1_16_R3.IRegistry;
import net.minecraft.server.v1_16_R3.IRegistryCustom;
import net.minecraft.server.v1_16_R3.MinecraftKey;
import net.minecraft.server.v1_16_R3.MobSpawner;
import net.minecraft.server.v1_16_R3.MobSpawnerCat;
import net.minecraft.server.v1_16_R3.MobSpawnerPatrol;
import net.minecraft.server.v1_16_R3.MobSpawnerPhantom;
import net.minecraft.server.v1_16_R3.MobSpawnerTrader;
import net.minecraft.server.v1_16_R3.NBTBase;
import net.minecraft.server.v1_16_R3.RegistryMaterials;
import net.minecraft.server.v1_16_R3.RegistryReadOps;
import net.minecraft.server.v1_16_R3.ResourceKey;
import net.minecraft.server.v1_16_R3.VillageSiege;
import net.minecraft.server.v1_16_R3.WorldDataServer;
import net.minecraft.server.v1_16_R3.WorldDimension;
import net.minecraft.server.v1_16_R3.WorldServer;
import net.minecraft.server.v1_16_R3.WorldSettings;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.craftbukkit.v1_16_R3.CraftServer;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.ThreadingUtil;
import org.kunlab.scenamatica.context.stage.StageWorldCreator;
import org.kunlab.scenamatica.exceptions.context.stage.StageCreateFailedException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class PatchedStageWorldCreator implements StageWorldCreator
{
    private static final int TIMEOUT = 6000;

    private final ScenamaticaRegistry registry;

    public PatchedStageWorldCreator(ScenamaticaRegistry registry)
    {
        this.registry = registry;
    }

    @Override
    public World createWorld(WorldCreator creator) throws StageCreateFailedException
    {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        WorldServer worldServer = createWorldServer(creator);
        WorldDataServer worldDataServer = worldServer.worldDataServer;

        craftServer.getServer().initWorld(
                worldServer,
                worldDataServer,
                worldDataServer,
                worldDataServer.getGeneratorSettings()
        );
        worldServer.setSpawnFlags(
                /* allowMonsters: */ worldServer.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING),
                /* allowAnimals: */ worldServer.getGameRules().getBoolean(GameRules.DO_MOB_SPAWNING)
        );

        craftServer.getServer().worldServer.put(worldServer.getDimensionKey(), worldServer);

        // キャンセル可能ロードを実施する。
       // loadSpawn(worldServer, TIMEOUT);

        craftServer.getPluginManager().callEvent(new WorldLoadEvent(worldServer.getWorld()));

        return worldServer.getWorld();
    }

    private void loadSpawn(WorldServer worldServer, long timeoutMillis)
    {
        ThreadingUtil.ThrowableSupplier<Void, Exception> supplier = () -> {
            ((CraftServer) Bukkit.getServer()).getServer().loadSpawn(
                    worldServer.getChunkProvider().playerChunkMap.worldLoadListener,
                    worldServer
            );
            return null;
        };

        try
        {
            CompletableFuture<Void> future = CompletableFuture.supplyAsync(() -> {
                try
                {
                    supplier.get();
                }
                catch (Exception e)
                {
                    throw new RuntimeException(e);
                }
                return null;
            });
            future.get(timeoutMillis, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException | ExecutionException | TimeoutException e)
        {
            throw new RuntimeException(e);
        }
    }


    private static WorldServer createWorldServer(WorldCreator creator) throws StageCreateFailedException
    {
        String name = creator.name();
        World.Environment environment = creator.environment();
        org.bukkit.generator.ChunkGenerator bukkitChunkGenerator = creator.generator();
        boolean isHardcore = creator.hardcore();

        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        if (bukkitChunkGenerator == null)
            bukkitChunkGenerator = craftServer.getGenerator(name);

        File folder = new File(Bukkit.getServer().getWorldContainer(), name);

        // World 環境周りの設定
        RegistryReadOps<NBTBase> registryReadOps = RegistryReadOps.a(
                DynamicOpsNBT.a,
                craftServer.getServer().dataPackResources.h(),
                (IRegistryCustom.Dimension) craftServer.getServer().getCustomRegistry()
        );

        WorldDataServer worldDataServer = createWorldDataServer(creator);
        // サーバに Mod があった場合の適用
        worldDataServer.a(craftServer.getServer().getServerModName(), craftServer.getServer().getModded().isPresent());

        // ワールドの生成周りの設定
        long normalizedSeed = normalizeSeed(creator.seed());
        List<MobSpawner> spawners = getSpawners(worldDataServer);
        WorldDimension resolvedDimension = resolveDimension(worldDataServer, environment);

        DimensionManager dimensionManager;
        ChunkGenerator nmsChunkGenerator;
        if (resolvedDimension == null)
        {
            dimensionManager = craftServer.getServer().customRegistry.a().d(DimensionManager.OVERWORLD);
            nmsChunkGenerator = GeneratorSettings.a(
                    craftServer.getServer().customRegistry.b(IRegistry.ay),
                    craftServer.getServer().customRegistry.b(IRegistry.ar),
                    new Random().nextLong()
            );
        }
        else
        {
            dimensionManager = resolvedDimension.b();
            nmsChunkGenerator = resolvedDimension.c();
        }

        Convertable.ConversionSession session = getSession(name, convertEnvironmentToNMSDimension(environment));
        ResourceKey<net.minecraft.server.v1_16_R3.World> worldKey = getWorldKey(name);
        return new WorldServer(
                craftServer.getServer(),
                craftServer.getServer().executorService,
                session,
                worldDataServer,
                worldKey,
                dimensionManager,
                craftServer.getServer().worldLoadListenerFactory.create(11),
                nmsChunkGenerator,
                /* isDebug: */ false,
                normalizedSeed,
                /* spawners: */ environment == World.Environment.NORMAL ? spawners : Collections.emptyList(),
                /* doDaylightCycle: */ true,
                environment,
                bukkitChunkGenerator
        );
    }

    private static Convertable.ConversionSession getSession(String name, ResourceKey<WorldDimension> dimension) throws StageCreateFailedException
    {
        try
        {
            return Convertable.a(Bukkit.getWorldContainer().toPath())
                    .c(name, dimension);
        }
        catch (IOException e)
        {
            throw new StageCreateFailedException(name, e);
        }
    }

    private static ResourceKey<net.minecraft.server.v1_16_R3.World> getWorldKey(String worldName)
    {
        return ResourceKey.a(IRegistry.L, new MinecraftKey(worldName.toLowerCase(Locale.ENGLISH)));
    }

    @Nullable
    private static WorldDimension resolveDimension(WorldDataServer worldDataServer, World.Environment environment)
    {
        RegistryMaterials<WorldDimension> materials = worldDataServer.getGeneratorSettings().d();
        ResourceKey<WorldDimension> dimension = convertEnvironmentToNMSDimension(environment);
        return materials.a(dimension);
    }

    private static List<MobSpawner> getSpawners(WorldDataServer worldDataServer)
    {
        return Collections.unmodifiableList(Arrays.asList(
                new MobSpawnerPhantom(),
                new MobSpawnerPatrol(),
                new MobSpawnerCat(),
                new VillageSiege(),
                new MobSpawnerTrader(worldDataServer)
        ));
    }

    private static long normalizeSeed(long seed)
    {
        return BiomeManager.a(seed);
    }

    private static WorldDataServer createWorldDataServer(WorldCreator creator)
    {
        // 本来は Convertable あたりの処理が必要だが, 新規作成なのでマイグレーションは発生しない
        String name = creator.name();
        boolean isHardcore = creator.hardcore();

        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        Properties properties = getCreationProperties(creator);
        WorldSettings settings = new WorldSettings(
                name,
                getDefaultGameMode(),
                isHardcore,
                EnumDifficulty.EASY,
                /* allowCommands: */ false,
                new GameRules(),
                craftServer.getServer().datapackconfiguration
        );
        GeneratorSettings generatorSettings = GeneratorSettings.a(
                craftServer.getServer().getCustomRegistry(),
                properties
        );

        return new WorldDataServer(settings, generatorSettings, Lifecycle.stable());
    }


    private static EnumGamemode getDefaultGameMode()
    {
        CraftServer craftServer = (CraftServer) Bukkit.getServer();
        GameMode defaultGameMode = craftServer.getDefaultGameMode();
        switch (defaultGameMode)
        {
            case SURVIVAL:
                return EnumGamemode.SURVIVAL;
            case CREATIVE:
                return EnumGamemode.CREATIVE;
            case ADVENTURE:
                return EnumGamemode.ADVENTURE;
            case SPECTATOR:
                return EnumGamemode.SPECTATOR;
            default:
                throw new IllegalArgumentException("Unknown default game mode: " + defaultGameMode);
        }
    }

    private static Properties getCreationProperties(WorldCreator creator)
    {
        Properties properties = new Properties();
        properties.put("generator-settings", creator.generatorSettings());
        properties.put("level-seed", String.valueOf(creator.seed()));
        properties.put("generate-structures", String.valueOf(creator.generateStructures()));
        properties.put("level-type", creator.type().getName());
        return properties;
    }

    private static ResourceKey<WorldDimension> convertEnvironmentToNMSDimension(World.Environment environment)
    {
        switch (environment)
        {
            case NORMAL:
                return WorldDimension.OVERWORLD;
            case NETHER:
                return WorldDimension.THE_NETHER;
            case THE_END:
                return WorldDimension.THE_END;
            default:
                throw new IllegalArgumentException("Unknown environment: " + environment);
        }
    }

}
