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
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.NMSMinecraftServer;
import org.kunlab.scenamatica.nms.types.NMSPlayerList;
import org.kunlab.scenamatica.nms.types.NMSWorldServer;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;
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
    private final ActorSettings settings;

    public PlayerMockerBase(ScenamaticaRegistry registry, ActorSettings settings)
    {
        this.registry = registry;
        this.settings = settings;
    }

    protected static GameProfile createGameProfile(PlayerStructure structure)
    {
        UUID uuid = structure.getUuid() == null ? UUID.randomUUID(): structure.getUuid();
        String name = structure.getName();

        return new GameProfile(uuid, name);
    }

    public abstract Actor mock(@NotNull World world, @NotNull PlayerStructure structure);

    public abstract void unmock(Actor player);

    protected abstract Class<?> getLoginListenerClass();

    public abstract void postActorLogin(Player player);

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

    protected void initActor(Player player, PlayerStructure structure)
    {
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

    protected void removePersistentPlayerData(Player player)
    {
        NMSMinecraftServer nmsServer = NMSProvider.getProvider().wrap(Bukkit.getServer());
        NMSPlayerList nmsPlayerList = nmsServer.getPlayerList();
        NMSEntityPlayer nmsPlayer = NMSProvider.getProvider().wrap(player);

        if (nmsPlayerList.isOp(nmsPlayer.getProfile()))
            nmsPlayerList.removeOp(nmsPlayer.getProfile());
        player.getEffectivePermissions().forEach(permissionAttachmentInfo -> {
            if (permissionAttachmentInfo.getAttachment() != null)
                player.removeAttachment(permissionAttachmentInfo.getAttachment());
        });
    }

    protected void moveToLocationSafe(Player player, Location initialLocation)
    {
        World initialWorld = initialLocation.getWorld();
        float yaw = initialLocation.getYaw();
        float pitch = initialLocation.getPitch();
        double x = initialLocation.getX();
        double y = initialLocation.getY();
        double z = initialLocation.getZ();

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
}
