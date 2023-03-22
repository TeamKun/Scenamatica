package net.kunmc.lab.scenamatica.context.actor;

import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.ActorManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.kunlab.kpm.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ActorManagerImpl implements ActorManager
{
    private final List<Player> mockedPlayers;
    private final PlayerMockerBase mocker;

    public ActorManagerImpl(ScenamaticaRegistry registry)
    {
        this.mockedPlayers = new ArrayList<>();
        this.mocker = getMocker(registry, this);
    }

    private static PlayerMockerBase getMocker(ScenamaticaRegistry registry, ActorManager manager)
    {
        String version = ReflectionUtils.PackageType.getServerVersion();
        //noinspection SwitchStatementWithTooFewBranches
        switch (version)  // TODO: Support other versions.
        {
            case "v1_16_R3":
                return new net.kunmc.lab.scenamatica.context.actor.nms.v_1_16_R3.PlayerMocker(registry, manager);
            default:
                throw new UnsupportedOperationException("Unsupported version: " + version);
        }
    }

    @Override
    public Player createActor(World stage, PlayerBean bean)
    {
        if (this.mockedPlayers.stream().anyMatch(p -> p.getName().equalsIgnoreCase(bean.getName())))
            throw new IllegalArgumentException("Player " + bean.getName() + " is already mocked.");

        Player player = this.mocker.mock(stage, bean);
        this.mockedPlayers.add(player);
        return player;
    }

    @Override
    public void destroyActor(Player player)
    {
        this.mocker.unmock(player);
    }

    @Override
    public void onDestroyActor(Player player)
    {
        this.mockedPlayers.remove(player);
    }

    @Override
    public void shutdown()
    {
        new ArrayList<>(this.mockedPlayers)  //   回避
                .forEach(this::destroyActor);
    }
}
