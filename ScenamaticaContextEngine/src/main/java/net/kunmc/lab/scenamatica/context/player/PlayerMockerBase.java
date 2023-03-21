package net.kunmc.lab.scenamatica.context.player;

import com.mojang.authlib.GameProfile;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.bukkit.entity.Player;

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
}
