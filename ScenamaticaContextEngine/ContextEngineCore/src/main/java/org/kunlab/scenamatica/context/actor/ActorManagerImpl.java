package org.kunlab.scenamatica.context.actor;

import lombok.Getter;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.ThreadingUtil;
import org.kunlab.scenamatica.events.actor.ActorPostJoinEvent;
import org.kunlab.scenamatica.exceptions.context.ContextPreparationException;
import org.kunlab.scenamatica.exceptions.context.actor.ActorAlreadyExistsException;
import org.kunlab.scenamatica.exceptions.context.actor.VersionNotSupportedException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;
import org.kunlab.scenamatica.settings.ActorSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActorManagerImpl implements ActorManager, Listener
{
    private final ScenamaticaRegistry registry;
    @Getter
    private final List<Actor> actors;
    private final PlayerMockerBase actorGenerator;
    private final ActorSettings settings;

    private final ConcurrentHashMap<UUID, Object> waitingForLogin;

    private boolean destroyed;

    public ActorManagerImpl(ScenamaticaRegistry registry) throws VersionNotSupportedException
    {
        this.registry = registry;
        this.settings = registry.getEnvironment().getActorSettings();
        this.actors = new ArrayList<>();
        this.actorGenerator = getMocker(registry, this);

        this.init();
        this.waitingForLogin = new ConcurrentHashMap<>();
    }

    private static PlayerMockerBase getMocker(ScenamaticaRegistry registry, ActorManager manager)
            throws VersionNotSupportedException
    {
        String version = getServerNMSVersion();
        //noinspection SwitchStatementWithTooFewBranches
        switch (version)  // TODO: Support other versions.
        {
            case "v1_16_R3":
                return new org.kunlab.scenamatica.context.actor.nms.v1_16_R3.PlayerMocker(registry, manager);
            default:
                throw new VersionNotSupportedException(version);
        }
    }

    private static String getServerNMSVersion()
    {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }

    private void init()
    {
        this.registry.getPlugin().getServer().getPluginManager().registerEvents(this, this.registry.getPlugin());
    }

    @Override
    public Actor createActor(@NotNull World defaultWorld, @NotNull PlayerStructure structure) throws ContextPreparationException
    {
        Objects.requireNonNull(structure.getName(), "Unable to create actor: name is null.");

        if (Bukkit.getServer().isPrimaryThread())
            throw new ContextPreparationException("This method must be called from another thread.");
        else if (this.actors.stream().anyMatch(p -> p.getName().equalsIgnoreCase(structure.getName())))
            throw new ActorAlreadyExistsException(structure.getName());
        else if (this.actors.size() + 1 > this.settings.getMaxActors())
            throw new ContextPreparationException("Too many actors on this server (max: " + this.settings.getMaxActors() + ")");

        Actor actor = ThreadingUtil.waitFor(
                this.registry,
                () -> this.actorGenerator.mock(defaultWorld, structure)
        );

        this.actors.add(actor);

        if (structure.getOnline() == null || structure.getOnline())  // オンラインモードはログインするの待つ。
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
        this.actors.remove(player);
    }

    @Override
    public void onDestroyActor(Actor player)
    {
        this.actorGenerator.onDestroyActor(player);
    }

    @Override
    public void shutdown()
    {
        if (this.destroyed)
            throw new IllegalStateException("Already destroyed.");

        this.destroyed = true;

        new ArrayList<>(this.actors)  //   回避
                .forEach(this::destroyActor);
    }

    @Override
    public boolean isActor(@NotNull Player player)
    {
        return this.actors.stream().anyMatch(p -> p.getUUID().equals(player.getUniqueId()));
    }

    @Override
    public Actor getByUUID(@NotNull UUID uuid)
    {
        return this.actors.stream()
                .filter(p -> p.getUUID().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    @Override
    public @Nullable Actor getByName(@NotNull String name)
    {
        return this.actors.stream()
                .filter(p -> p.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onActorJoin(ActorPostJoinEvent e)
    {
        Actor actor = e.getActor();
        Player player = actor.getPlayer();
        this.actorGenerator.postActorLogin(actor);
        Object locker = this.waitingForLogin.remove(player.getUniqueId());

        if (!Objects.nonNull(locker))
            return;
        synchronized (locker)
        {
            locker.notify();
        }

    }

}
