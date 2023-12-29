package org.kunlab.scenamatica.action;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.utils.EventListenerUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatcherManager;
import org.kunlab.scenamatica.interfaces.action.WatchingEntry;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WatcherManagerImpl implements WatcherManager
{
    private static final Listener DUMMY_LISTENER;

    static
    {
        DUMMY_LISTENER = new Listener()
        {
            final String NAME = "Scenamatica Dummy Listener";
        };
    }

    private final ScenamaticaRegistry registry;
    private final Object lock = new Object();
    private final Multimap<Plugin, WatchingEntryImpl> actionWatchers;

    public WatcherManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.actionWatchers = ArrayListMultimap.create();
    }

    @Override
    public List<WatchingEntry> registerWatchers(@NotNull ScenarioEngine engine,
                                                @NotNull ScenarioFileStructure scenario,
                                                @NotNull List<? extends CompiledAction> watchers,
                                                @NotNull WatchType type)
    {
        List<WatchingEntry> entries = new ArrayList<>();
        for (CompiledAction watcher : watchers)
            entries.add(this.createWatchingEntry(
                            engine,
                            watcher,
                            scenario,
                            type
                    )
            );

        synchronized (this.lock)
        {
            for (WatchingEntry entry : entries)
                this.actionWatchers.put(engine.getPlugin(), (WatchingEntryImpl) entry);
        }

        return entries;
    }

    @Override
    public WatchingEntry registerWatcher(@NotNull ScenarioEngine engine,
                                         @NotNull CompiledAction watcher,
                                         @NotNull ScenarioFileStructure scenario,
                                         @NotNull WatchType type)
    {
        WatchingEntryImpl entry = this.createWatchingEntry(
                engine,
                watcher,
                scenario,
                type
        );

        synchronized (this.lock)
        {
            this.actionWatchers.put(engine.getPlugin(), entry);
        }

        return entry;
    }

    private WatchingEntryImpl createWatchingEntry(@NotNull ScenarioEngine engine,
                                                  @NotNull CompiledAction action,
                                                  @NotNull ScenarioFileStructure scenario,
                                                  @NotNull WatchType type)
    {
        Action actionExecutor = action.getExecutor();
        if (!(actionExecutor instanceof Watchable))
            throw new IllegalStateException("The action " + actionExecutor.getName() + " is not watchable.");

        List<Pair<Class<? extends Event>, RegisteredListener>> listeners = new ArrayList<>();
        WatchingEntryImpl watchingEntry = new WatchingEntryImpl(
                this,
                engine,
                scenario,
                action,
                type,
                listeners
        );

        Watchable watchable = (Watchable) actionExecutor;

        for (Class<? extends Event> eventType : watchable.getAttachingEvents())
            listeners.add(Pair.of(eventType, this.register(watchingEntry, eventType)));

        return watchingEntry;
    }

    @Override
    public void unregisterWatchers(@NotNull Plugin plugin)
    {
        if (!this.actionWatchers.containsKey(plugin))
            throw new IllegalStateException("The plugin " + plugin.getName() + " is not registered.");

        synchronized (this.lock)
        {
            Collection<WatchingEntryImpl> entries = this.actionWatchers.get(plugin);
            for (WatchingEntryImpl entry : new ArrayList<>(entries))
                this.unregister(entry);
        }

    }

    @Override
    public void unregisterWatchers(@NotNull Plugin plugin, @NotNull WatchType type)
    {
        synchronized (this.lock)
        {
            if (!this.actionWatchers.containsKey(plugin))
                return;

            for (WatchingEntryImpl entry : new ArrayList<>(this.actionWatchers.get(plugin)))
            {
                if (entry.getType() == type)
                    this.unregister(entry);
            }
        }
    }

    public void onActionFired(@NotNull WatchingEntryImpl entry)
    {
        synchronized (this.lock)
        {
            if (!this.actionWatchers.containsValue(entry))
                throw new IllegalStateException("Unrecognized entry.");
        }

        if (entry.getType() == WatchType.TRIGGER)
        {
            try
            {
                this.registry.getTriggerManager().performTriggerFire(
                        entry.getEngine().getPlugin(),
                        entry.getScenario().getName(),
                        TriggerType.ON_ACTION,
                        entry.getAction().getContext().getInput()
                );
            }
            catch (ScenarioException e)
            {
                this.registry.getExceptionHandler().report(e);
            }

            return;
        }

        assert entry.getType() == WatchType.SCENARIO;

        this.unregister(entry);  // シナリオのアクションは一度だけ実行するようにする。(トリガは複数)

        entry.getEngine().getListener().onActionFinished(ActionResultImpl.fromAction(entry.getAction()), ScenarioType.CONDITION_REQUIRE);

        synchronized (this.lock)
        {
            this.actionWatchers.remove(entry.getEngine().getPlugin(), entry);
        }
    }

    private RegisteredListener register(WatchingEntryImpl entry, Class<? extends Event> eventType)
    {
        Action actionExecutor = entry.getAction().getExecutor();
        if (!(actionExecutor instanceof Watchable))
            throw new IllegalStateException("The action " + actionExecutor.getName() + " is not watchable.");

        EventExecutor executor = (listener1, event) -> this.onEventFire(entry, event);
        RegisteredListener registeredListener = new RegisteredListener(
                DUMMY_LISTENER,
                executor,
                EventPriority.LOWEST,  // ほかのイベントハンドラによる改変を許可。
                entry.getEngine().getPlugin(),
                true  // キャンセルされたら反応しないようにする。
        );

        EventListenerUtils.getListeners(eventType).register(registeredListener);

        return registeredListener;
    }

    private void onEventFire(WatchingEntryImpl entry, Event evt)
    {
        Action actionExecutor = entry.getAction().getExecutor();
        assert actionExecutor instanceof Watchable;
        Watchable watchable = (Watchable) actionExecutor;

        // 引数にマッチしているかどうかをチェックする。
        if (watchable.checkFired(entry.getAction().getContext(), evt))
            this.onActionFired(entry);
    }

    private void unregister(WatchingEntryImpl entry)
    {
        for (Pair<Class<? extends Event>, RegisteredListener> listenerPair : entry.getListeners())
            EventListenerUtils.getListeners(listenerPair.getLeft()).unregister(listenerPair.getRight());

        this.actionWatchers.remove(entry.getEngine().getPlugin(), entry);
    }
}
