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
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.EventListenerUtils;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.exceptions.scenario.BrokenReferenceException;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatcherManager;
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
    private final Multimap<Plugin, WatchEntry> actionWatchers;

    public WatcherManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.actionWatchers = ArrayListMultimap.create();
    }

    @Override
    public void registerWatchers(@NotNull ScenarioEngine engine,
                                 @NotNull ScenarioFileStructure scenario,
                                 @NotNull List<? extends CompiledAction> watchers,
                                 @NotNull WatchType type)
    {
        List<WatchEntry> entries = new ArrayList<>();
        for (int i = 0; i < watchers.size(); i++)
            entries.add(this.createWatchingEntry(
                    engine,
                    watchers.get(i),
                    scenario,
                    i,
                    type
            ));

        synchronized (this.lock)
        {
            for (WatchEntry entry : entries)
                this.actionWatchers.put(engine.getPlugin(), entry);
        }
    }

    @Override
    public void registerWatcher(@NotNull ScenarioEngine engine,
                                @NotNull CompiledAction watcher,
                                @NotNull ScenarioFileStructure scenario,
                                @NotNull WatchType type)
    {
        WatchEntry entry = this.createWatchingEntry(
                engine,
                watcher,
                scenario,
                this.getLastIdxOf(engine, type) + 1,
                type
        );

        synchronized (this.lock)
        {
            this.actionWatchers.put(engine.getPlugin(), entry);
        }
    }

    private int getLastIdxOf(@NotNull ScenarioEngine engine, @NotNull WatchType type)
    {
        return this.actionWatchers.get(engine.getPlugin()).stream()
                .filter(entry -> entry.getEngine() == engine)
                .filter(entry -> entry.getType() == type)
                .mapToInt(WatchEntry::getIdx)
                .max()
                .orElse(0);
    }

    private WatchEntry createWatchingEntry(@NotNull ScenarioEngine engine,
                                           @NotNull CompiledAction action,
                                           @NotNull ScenarioFileStructure scenario,
                                           int idx,
                                           @NotNull WatchType type)
    {
        Action actionExecutor = action.getExecutor();
        if (!(actionExecutor instanceof Watchable))
            throw new IllegalStateException("The action " + actionExecutor.getName() + " is not watchable.");

        List<Pair<Class<? extends Event>, RegisteredListener>> listeners = new ArrayList<>();
        WatchEntry watchEntry = new WatchEntry(
                this,
                engine,
                scenario,
                idx,
                action,
                type,
                listeners
        );

        Watchable watchable = (Watchable) actionExecutor;

        for (Class<? extends Event> eventType : watchable.getAttachingEvents())
            listeners.add(Pair.of(eventType, this.registerListener(watchEntry, eventType)));

        return watchEntry;
    }

    @Override
    public void unregisterWatchers(@NotNull Plugin plugin)
    {
        if (!this.actionWatchers.containsKey(plugin))
            throw new IllegalStateException("The plugin " + plugin.getName() + " is not registered.");

        synchronized (this.lock)
        {
            Collection<WatchEntry> entries = this.actionWatchers.get(plugin);
            for (WatchEntry entry : new ArrayList<>(entries))
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

            for (WatchEntry entry : new ArrayList<>(this.actionWatchers.get(plugin)))
            {
                if (entry.getType() == type)
                    this.unregister(entry);
            }
        }
    }

    public void onActionFired(@NotNull WatchEntry entry, boolean isJumped)
    {
        synchronized (this.lock)
        {
            if (!this.actionWatchers.containsValue(entry))
                return;
        }

        switch (entry.getType())
        {
            case SCENARIO:
                this.onScenarioFired(entry, isJumped);
                break;
            case TRIGGER:
                this.onTriggerFired(entry);
                break;
            default:
                throw new IllegalStateException("Unknown watch type: " + entry.getType());
        }
    }

    private void onScenarioFired(@NotNull WatchEntry entry, boolean isJumped)
    {
        entry.setEnabled(false);  // シナリオのアクションは一度だけ実行するようにする。(トリガは複数)

        entry.getEngine().getListener().onObservingActionExecuted(
                ActionResultImpl.fromAction(entry.getAction()),
                isJumped
        );
    }

    private void onTriggerFired(@NotNull WatchEntry entry)
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

    }

    private RegisteredListener registerListener(WatchEntry entry, Class<? extends Event> eventType)
    {
        Action actionExecutor = entry.getAction().getExecutor();
        if (!(actionExecutor instanceof Watchable))
            throw new IllegalStateException("The action " + actionExecutor.getName() + " is not watchable.");

        EventExecutor executor = (listener1, event) -> this.onEventFired(entry, event);
        RegisteredListener registeredListener = new RegisteredListener(
                DUMMY_LISTENER,
                executor,
                EventPriority.MONITOR,  // ほかのイベントハンドラによる改変を許可。
                entry.getEngine().getPlugin(),
                true  // キャンセルされたら反応しないようにする。
        );

        EventListenerUtils.getListeners(eventType).register(registeredListener);

        return registeredListener;
    }

    private void onEventFired(WatchEntry entry, Event evt)
    {
        CompiledAction action = entry.getAction();
        Action actionExecutor = action.getExecutor();
        assert actionExecutor instanceof Watchable;
        Watchable watchable = (Watchable) actionExecutor;

        boolean isJumped = this.checkJumped(entry);

        // 引数の解決を試みる。（この状態では、引数の解決が完了していない可能性がある。）
        BrokenReferenceException resolveError = this.tryResolve(entry);
        if (resolveError != null)
        {
            // 引数の解決に失敗した場合のケースとして：
            // 1. ジャンプが発生した場合（ジャンプ間にその変数を埋めることが想定されていた？)
            // 2. 本当に変数がない場合。
            // 前者は許容されるべきであるが、後者は許容されない。
            if (!isJumped)
                entry.getEngine().getListener().onActionError(entry.getAction(), resolveError);
            return;
        }

        try
        {
            // 引数にマッチしているかどうかをチェックする。
            if (watchable.checkFired(action.getContext(), evt))
                this.onActionFired(entry, isJumped);
        }
        catch (Throwable e)
        {
            entry.getEngine().getListener().onActionError(entry.getAction(), e);
        }
    }

    @Nullable
    private BrokenReferenceException tryResolve(@NotNull WatchEntry entry)
    {
        try
        {
            entry.getEngine().getExecutor().resolveInputs(entry.getAction());
            return null;
        }
        catch (BrokenReferenceException e)
        {
            return e;
        }
    }

    private boolean checkJumped(@NotNull WatchEntry executedEntry)
    {
        return this.actionWatchers.get(executedEntry.getEngine().getPlugin()).stream()
                .filter(entry -> entry.getType() == WatchType.SCENARIO)  // ジャンプはシナリオ内でしか発生しない。
                .filter(WatchEntry::isEnabled)
                .anyMatch(entry -> entry.getIdx() < executedEntry.getIdx());  // これ以降のアクションが実行されたかどうかをチェックする。
    }

    private void unregister(WatchEntry entry)
    {
        for (Pair<Class<? extends Event>, RegisteredListener> listenerPair : entry.getListeners())
            EventListenerUtils.getListeners(listenerPair.getLeft()).unregister(listenerPair.getRight());

        synchronized (this.lock)
        {
            this.actionWatchers.remove(entry.getEngine().getPlugin(), entry);
        }
    }
}
