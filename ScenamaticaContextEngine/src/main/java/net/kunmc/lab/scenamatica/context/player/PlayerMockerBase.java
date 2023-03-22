package net.kunmc.lab.scenamatica.context.player;

import com.mojang.authlib.GameProfile;
import lombok.SneakyThrows;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.apache.logging.log4j.LogManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerLoginEvent;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public abstract class PlayerMockerBase
{
    protected static GameProfile createGameProfile(PlayerBean bean)
    {
        UUID uuid = bean.getUuid() == null ? UUID.randomUUID(): bean.getUuid();
        String name = bean.getName() == null ? "Player_" + uuid.toString().substring(0, 8): bean.getName();

        return new GameProfile(uuid, name);
    }

    public abstract Player mock(PlayerBean bean);

    public abstract void unmock(Player player);

    protected abstract Class<?> getLoginListenerClass();

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
                    .info("Disconnecting {}: {}", socketAddr, event.kickMessage());
        }

        return event.getResult() == PlayerLoginEvent.Result.ALLOWED;
    }
}
