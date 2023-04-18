package net.kunmc.lab.scenamatica.context.actor;

import lombok.Getter;
import net.kunmc.lab.scenamatica.exceptions.context.ContextPreparationException;
import net.kunmc.lab.scenamatica.exceptions.context.actor.ActorAlreadyExistsException;
import net.kunmc.lab.scenamatica.exceptions.context.actor.VersionNotSupportedException;
import net.kunmc.lab.scenamatica.exceptions.context.stage.StageNotCreatedException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.ActorManager;
import net.kunmc.lab.scenamatica.interfaces.context.ContextManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import net.kunmc.lab.scenamatica.settings.ActorSettings;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.kunlab.kpm.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;

public class ActorManagerImpl implements ActorManager, Listener
{
    private final ScenamaticaRegistry registry;
    private final ContextManager contextManager;
    @Getter
    private final List<Player> actors;
    private final PlayerMockerBase actorGenerator;
    private final ActorSettings settings;

    public ActorManagerImpl(ScenamaticaRegistry registry, ContextManager contextManager) throws VersionNotSupportedException
    {
        this.registry = registry;
        this.contextManager = contextManager;
        this.settings = registry.getEnvironment().getActorSettings();
        this.actors = new ArrayList<>();
        this.actorGenerator = getMocker(registry, this);

        this.init();
    }

    private void init()
    {
        this.registry.getPlugin().getServer().getPluginManager().registerEvents(this, this.registry.getPlugin());
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
    public Player createActor(PlayerBean bean) throws ContextPreparationException
    {
        if (this.actors.stream().anyMatch(p -> p.getName().equalsIgnoreCase(bean.getName())))
            throw new ActorAlreadyExistsException(bean.getName());
        else if (!this.contextManager.getStageManager().isStageCreated())
            throw new StageNotCreatedException();
        else if (this.actors.size() + 1 > this.settings.getMaxActors())
            throw new ContextPreparationException("Too many actors on this server (max: " + this.settings.getMaxActors() + ")");

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

    @EventHandler
    public void onActorJoin(PlayerJoinEvent e)
    {
        if (this.isActor(e.getPlayer()))
            this.actorGenerator.postActorLogin(e.getPlayer());
    }

}
