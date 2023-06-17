package net.kunmc.lab.scenamatica.context.actor;

import lombok.Getter;
import lombok.SneakyThrows;
import net.kunmc.lab.scenamatica.commons.utils.ThreadingUtil;
import net.kunmc.lab.scenamatica.events.actor.ActorPostJoinEvent;
import net.kunmc.lab.scenamatica.exceptions.context.ContextPreparationException;
import net.kunmc.lab.scenamatica.exceptions.context.actor.ActorAlreadyExistsException;
import net.kunmc.lab.scenamatica.exceptions.context.actor.VersionNotSupportedException;
import net.kunmc.lab.scenamatica.exceptions.context.stage.StageNotCreatedException;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.context.Actor;
import net.kunmc.lab.scenamatica.interfaces.context.ActorManager;
import net.kunmc.lab.scenamatica.interfaces.context.ContextManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import net.kunmc.lab.scenamatica.settings.ActorSettings;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.kpm.utils.ReflectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActorManagerImpl implements ActorManager, Listener
{
    private final ScenamaticaRegistry registry;
    private final ContextManager contextManager;
    @Getter
    private final List<Actor> actors;
    private final PlayerMockerBase actorGenerator;
    private final ActorSettings settings;

    private final ConcurrentHashMap<UUID, Object> waitingForLogin;

    public ActorManagerImpl(ScenamaticaRegistry registry, ContextManager contextManager) throws VersionNotSupportedException
    {
        this.registry = registry;
        this.contextManager = contextManager;
        this.settings = registry.getEnvironment().getActorSettings();
        this.actors = new ArrayList<>();
        this.actorGenerator = getMocker(registry, this);

        this.init();
        this.waitingForLogin = new ConcurrentHashMap<>();
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

    private void init()
    {
        this.registry.getPlugin().getServer().getPluginManager().registerEvents(this, this.registry.getPlugin());
    }

    @Override
    public Actor createActor(PlayerBean bean) throws ContextPreparationException
    {
        if (Bukkit.getServer().isPrimaryThread())
            throw new ContextPreparationException("This method must be called from another thread.");
        else if (this.actors.stream().anyMatch(p -> p.getName().equalsIgnoreCase(bean.getName())))
            throw new ActorAlreadyExistsException(bean.getName());
        else if (!this.contextManager.getStageManager().isStageCreated())
            throw new StageNotCreatedException();
        else if (this.actors.size() + 1 > this.settings.getMaxActors())
            throw new ContextPreparationException("Too many actors on this server (max: " + this.settings.getMaxActors() + ")");

        Actor actor = ThreadingUtil.waitFor(this.registry, () -> this.actorGenerator.mock(this.contextManager.getStageManager().getStage(), bean));

        this.actors.add(actor);

        if (bean.isOnline())  // オンラインモードはログインするの待つ。
            this.waitForJoin(actor);

        return actor;
    }

    @SneakyThrows(InterruptedException.class)
    private void waitForJoin(Actor actor)
    {
        // ログイン処理は Bukkit がメインスレッドで行う必要があるため, ここでは帰ってこない。
        // イベントをリッスンして, ログインしたら待機しているスレッドを起こす必要がある。
        Object locker = new Object();
        this.waitingForLogin.put(actor.getUUID(), locker);
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (locker)
        {
            locker.wait();
        }
    }

    @Override
    public void destroyActor(Actor player)
    {
        this.actorGenerator.unmock(player);
    }

    @Override
    public void onDestroyActor(Actor player)
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
    public boolean isActor(@NotNull Player player)
    {
        return this.actors.stream().parallel()
                .anyMatch(p -> p.getUUID().equals(player.getUniqueId()));
    }

    @Override
    public Actor getByUUID(@NotNull UUID uuid)
    {
        return this.actors.stream().parallel()
                .filter(p -> p.getUUID().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    @Override
    public @Nullable Actor getByName(@NotNull String name)
    {
        return this.actors.stream().parallel()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onActorJoin(ActorPostJoinEvent e)
    {
        Actor actor = e.getActor();
        Player player = actor.getPlayer();
        this.actorGenerator.postActorLogin(player);
        Object locker = this.waitingForLogin.remove(player.getUniqueId());

        if (!Objects.nonNull(locker))
            return;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (locker)
        {
            locker.notify();
        }

    }

}
