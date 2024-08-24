package org.kunlab.scenamatica.context.actor;

import com.mojang.authlib.GameProfile;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.NMSMinecraftServer;
import org.kunlab.scenamatica.nms.types.NMSPlayerList;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;
import org.kunlab.scenamatica.nms.types.player.NMSPlayerConnection;
import org.kunlab.scenamatica.nms.types.world.NMSWorldServer;
import org.kunlab.scenamatica.settings.ActorSettings;

import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public abstract class PlayerMockerBase
{
    private final ScenamaticaRegistry registry;
    private final ActorManager manager;
    private final ActorSettings settings;

    public PlayerMockerBase(ScenamaticaRegistry registry, ActorManager manager, ActorSettings settings)
    {
        this.registry = registry;
        this.manager = manager;
        this.settings = settings;
    }

    protected static GameProfile createGameProfile(PlayerStructure structure)
    {
        UUID uuid = structure.getUuid() == null ? UUID.randomUUID(): structure.getUuid();
        String name = structure.getName();

        return new GameProfile(uuid, name);
    }

    private static void removePersistentPlayerData(NMSPlayerList nmsPlayerList, NMSEntityPlayer nmsPlayer, Player player)
    {
        if (nmsPlayerList.isOp(nmsPlayer.getProfile()))
            nmsPlayerList.removeOp(nmsPlayer.getProfile());
        player.getEffectivePermissions().forEach(permissionAttachmentInfo -> {
            if (permissionAttachmentInfo.getAttachment() != null)
                player.removeAttachment(permissionAttachmentInfo.getAttachment());
        });
    }

    protected static Location createInitialLocation(World world, PlayerStructure structure)
    {
        Location initialLocation;
        if (structure.getLocation() == null)
            initialLocation = world.getSpawnLocation().clone();
        else
            initialLocation = structure.getLocation().create();

        if (structure.getLocation() != null && structure.getLocation().getWorld() == null)
            initialLocation.setWorld(world);

        return initialLocation;
    }

    public Actor mock(@NotNull World world, @NotNull PlayerStructure structure, boolean doLogin)
    {
        Actor actor = this.createActorInstance(world, structure);
        boolean actualDoLogin = doLogin && (structure.getOnline() == null || structure.getOnline());
        if (actualDoLogin)
            this.doLogin(actor);

        return actor;
    }

    public abstract void doLogin(Actor actor);

    public void unmock(Actor actor)
    {
        Player player = actor.getPlayer();

        NMSMinecraftServer nmsServer = NMSProvider.getProvider().wrap(Bukkit.getServer());
        NMSPlayerList nmsPlayerList = nmsServer.getPlayerList();
        NMSEntityPlayer nmsPlayer = NMSProvider.getProvider().wrap(player);
        removePersistentPlayerData(nmsPlayerList, nmsPlayer, player);

        NMSPlayerConnection nmsConnection = nmsPlayer.getConnection();
        if (nmsConnection == null)
            return;

        nmsConnection.disconnect("Disconnected");
    }

    protected boolean dispatchLoginEvent(Actor player, InetSocketAddress addr)
    {
        PlayerLoginEvent event = new PlayerLoginEvent(
                player.getPlayer(),
                addr.getHostName(),
                addr.getAddress()
        );
        Bukkit.getPluginManager().callEvent(event);

        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED)
        {
            // LoginListener 名義のログを偽装
            LogManager.getLogger(this.getLoginListenerClass())
                    .info("Disconnecting {}: {}", player.getPlayer().getAddress(), event.getKickMessage());
        }

        return event.getResult() == PlayerLoginEvent.Result.ALLOWED;
    }

    private void initActor(Actor actor)
    {
        Player player = actor.getPlayer();
        PlayerStructure structure = actor.getInitialStructure();

        structure.applyTo(player);
        Stream.of(structure.getTags(), this.settings.getDefaultScoreboardTags())
                .flatMap(List::stream)
                .distinct()
                .forEach(player::addScoreboardTag);
        Stream.of(structure.getActivePermissions(), this.settings.getDefaultPermissions())
                .flatMap(List::stream)
                .distinct()
                .forEach(permission -> player.addAttachment(this.registry.getPlugin(), permission, true));
    }

    protected void moveToLocationSafe(Player player, Location initialLocation)
    {
        World initialWorld = initialLocation.getWorld();
        if (initialWorld == player.getWorld())
        {
            player.teleport(initialLocation);
            return;
        }

        // ワールドの変更には PlayerList#moveToWorld を使う

        NMSMinecraftServer nmsServer = NMSProvider.getProvider().wrap(Bukkit.getServer());
        NMSPlayerList nmsPlayerList = nmsServer.getPlayerList();
        NMSEntityPlayer nmsPlayer = NMSProvider.getProvider().wrap(player);
        NMSWorldServer world = NMSProvider.getProvider().wrap(initialWorld);

        Runner.run(() -> {  // Removing entity while ticking! 回避
            nmsPlayerList.moveToWorld(
                    nmsPlayer,
                    world,
                    true,
                    initialLocation,
                    true
            );
        });
    }

    public void onDestroyActor(Actor actor)
    {
        this.wipePlayerData(actor.getPlayer().getUniqueId());
    }

    public void wipePlayerData(UUID uuid)
    {
        try
        {
            for (World world : Bukkit.getWorlds())
            {
                Path worldDir = world.getWorldFolder().toPath().resolve("playerdata");
                Path playerDataInWorld = worldDir.resolve(uuid + ".dat");
                Path playerDataOldInWorld = worldDir.resolve(uuid + ".dat_old");
                Files.deleteIfExists(playerDataInWorld);
                Files.deleteIfExists(playerDataOldInWorld);

                Path advancementsDir = world.getWorldFolder().toPath().resolve("advancements");
                Path advancementsInWorld = advancementsDir.resolve(uuid + ".json");

                Files.deleteIfExists(advancementsInWorld);
            }
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);
        }
    }

    public void postActorLogin(@NotNull Actor actor)
    {
        this.injectPlayerConnection(actor.getPlayer());
        this.initActor(actor);
        this.moveToLocationSafe(actor.getPlayer(), actor.getInitialLocation());
    }

    protected abstract void sendSettings(@NotNull Player player);

    protected abstract Actor createActorInstance(@NotNull World world, @NotNull PlayerStructure structure);

    protected abstract Class<?> getLoginListenerClass();

    protected abstract void injectPlayerConnection(@NotNull Player player);
}
