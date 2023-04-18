package net.kunmc.lab.scenamatica.context.actor;

import com.mojang.authlib.GameProfile;
import lombok.SneakyThrows;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.ActorManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;
import org.jetbrains.annotations.NotNull;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public abstract class PlayerMockerBase
{
    private final ScenamaticaRegistry registry;
    private final ActorManager manager;

    public PlayerMockerBase(ScenamaticaRegistry registry, ActorManager manager)
    {
        this.registry = registry;
        this.manager = manager;
    }

    protected static GameProfile createGameProfile(PlayerBean bean)
    {
        UUID uuid = bean.getUuid() == null ? UUID.randomUUID(): bean.getUuid();
        String name = bean.getName();

        return new GameProfile(uuid, name);
    }

    public abstract Player mock(@NotNull World world, @NotNull PlayerBean bean);

    public abstract void unmock(Player player);

    protected abstract Class<?> getLoginListenerClass();

    public abstract void postActorLogin(Player player);

    @SneakyThrows(UnknownHostException.class)
    protected boolean dispatchLoginEvent(Player player)
    {
        InetAddress addr = InetAddress.getByName("191.9.81.0");
        InetSocketAddress socketAddr = new InetSocketAddress(addr, 1919);

        PlayerLoginEvent event = new PlayerLoginEvent(player, "191.9.81.0", addr);
        Bukkit.getPluginManager().callEvent(event);

        if (event.getResult() != PlayerLoginEvent.Result.ALLOWED)
        {
            // LoginListener 名義のログを偽装
            LogManager.getLogger(getLoginListenerClass())
                    .info("Disconnecting {}: {}", socketAddr, event.getKickMessage());
        }

        return event.getResult() == PlayerLoginEvent.Result.ALLOWED;
    }

    public void onDestroyActor(Player player)
    {
        this.manager.onDestroyActor(player);
        this.wipePlayerData(player.getUniqueId());
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
