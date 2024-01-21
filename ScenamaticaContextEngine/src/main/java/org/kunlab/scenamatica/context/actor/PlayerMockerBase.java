package org.kunlab.scenamatica.context.actor;

import com.mojang.authlib.GameProfile;
import lombok.SneakyThrows;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public abstract class PlayerMockerBase
{
    public static final String DEFAULT_IP = "114.51.48.10";
    public static final Integer DEFAULT_PORT = 1919;
    public static final String DEFAULT_HOST_NAME = DEFAULT_IP;

    private final ScenamaticaRegistry registry;
    private final ActorManager manager;

    public PlayerMockerBase(ScenamaticaRegistry registry, ActorManager manager)
    {
        this.registry = registry;
        this.manager = manager;
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

    @SneakyThrows(UnknownHostException.class)
    protected boolean dispatchLoginEvent(Actor player)
    {
        InetAddress addr;
        InetSocketAddress socketAddr;
        String hostName;
        if (player.getInitialStructure().getRemoteAddress() == null)
            addr = InetAddress.getByName(DEFAULT_IP);
        else
            addr = player.getInitialStructure().getRemoteAddress();
        if (player.getInitialStructure().getPort() == null)
            socketAddr = new InetSocketAddress(addr, DEFAULT_PORT);
        else
            socketAddr = new InetSocketAddress(addr, player.getInitialStructure().getPort());
        if (player.getInitialStructure().getHostName() == null)
            hostName = DEFAULT_HOST_NAME;
        else
            hostName = player.getInitialStructure().getHostName();


        PlayerLoginEvent event = new PlayerLoginEvent(player.getPlayer(), hostName, addr);
        Bukkit.getPluginManager().callEvent(event);

        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED)
        {
            // LoginListener 名義のログを偽装
            LogManager.getLogger(this.getLoginListenerClass())
                    .info("Disconnecting {}: {}", socketAddr, event.kickMessage());
        }

        return event.getResult() == PlayerLoginEvent.Result.ALLOWED;
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
