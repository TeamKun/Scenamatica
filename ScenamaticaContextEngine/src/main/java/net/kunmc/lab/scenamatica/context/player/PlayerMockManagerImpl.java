package net.kunmc.lab.scenamatica.context.player;

import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.interfaces.PlayerMockManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.bukkit.entity.Player;
import org.kunlab.kpm.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

public class PlayerMockManagerImpl implements PlayerMockManager
{
    private final List<Player> mockedPlayers;
    private final PlayerMockerBase mocker;

    public PlayerMockManagerImpl(ScenamaticaRegistry registry)
    {
        this.mockedPlayers = new ArrayList<>();
        this.mocker = getMocker(registry);
    }

    private static PlayerMockerBase getMocker(ScenamaticaRegistry registry)
    {
        String version = ReflectionUtils.PackageType.getServerVersion();
        //noinspection SwitchStatementWithTooFewBranches
        switch (version)  // TODO: Support other versions.
        {
            case "v1_16_R3":
                return new net.kunmc.lab.scenamatica.context.player.nms.v_1_16_R3.PlayerMocker(registry);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + version);
        }
    }

    @Override
    public Player mock(PlayerBean bean)
    {
        Player player = this.mocker.mock(bean);
        this.mockedPlayers.add(player);
        return player;
    }

    @Override
    public void unmock(Player player)
    {
        this.mocker.unmock(player);
        this.mockedPlayers.remove(player);
    }

    @Override
    public void shutdown()
    {
        new ArrayList<>(this.mockedPlayers)  //   回避
                .forEach(this::unmock);
    }
}
