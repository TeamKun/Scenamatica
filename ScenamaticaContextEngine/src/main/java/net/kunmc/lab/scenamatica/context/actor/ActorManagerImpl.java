package net.kunmc.lab.scenamatica.context.actor;

import lombok.Getter;
import net.kunmc.lab.scenamatica.exceptions.context.actor.ActorAlreadyExistsException;
import net.kunmc.lab.scenamatica.exceptions.context.actor.VersionNotSupportedException;
import net.kunmc.lab.scenamatica.exceptions.context.stage.StageNotCreatedException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.ActorManager;
import net.kunmc.lab.scenamatica.interfaces.context.ContextManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.bukkit.entity.Player;
import org.kunlab.kpm.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ActorManagerImpl implements ActorManager
{
    private final ContextManager contextManager;
    @Getter
    private final List<Player> actors;
    private final PlayerMockerBase actorGenerator;

    public ActorManagerImpl(ScenamaticaRegistry registry, ContextManager contextManager) throws VersionNotSupportedException
    {
        this.contextManager = contextManager;
        this.actors = new ArrayList<>();
        this.actorGenerator = getMocker(registry, this);
    }

    private static PlayerMockerBase getMocker(ScenamaticaRegistry registry, ActorManager manager)
            throws VersionNotSupportedException
    {
        String version = ReflectionUtils.PackageType.getServerVersion();
        //noinspection SwitchStatementWithTooFewBranches
        switch (version)  // TODO: Support other versions.
        {
            case "v1_16_R3":
                return new net.kunmc.lab.scenamatica.context.actor.nms.v_1_16_R3.PlayerMocker(registry, manager);
            default:
                throw new VersionNotSupportedException(version);
        }
    }

    @Override
    public Player createActor(PlayerBean bean) throws ActorAlreadyExistsException, StageNotCreatedException
    {
        if (this.actors.stream().anyMatch(p -> p.getName().equalsIgnoreCase(bean.getName())))
            throw new ActorAlreadyExistsException(bean.getName());
        else if (!this.contextManager.getStageManager().isStageCreated())
            throw new StageNotCreatedException();

        Player player = this.actorGenerator.mock(this.contextManager.getStageManager().getStage(), bean);
        this.actors.add(player);
        return player;
    }

    @Override
    public void destroyActor(Player player)
    {
        this.actorGenerator.unmock(player);
    }

    @Override
    public void onDestroyActor(Player player)
    {
        this.actors.remove(player);
    }

    @Override
    public void shutdown()
    {
        new ArrayList<>(this.actors)  //   回避
                .forEach(this::destroyActor);
    }

    @Override
    public boolean isActor(Player player)
    {
        return this.actors.stream().parallel()
                .anyMatch(p -> p.getUniqueId().equals(player.getUniqueId()));
    }
}
