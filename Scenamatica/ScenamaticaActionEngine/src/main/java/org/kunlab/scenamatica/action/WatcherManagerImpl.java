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
import org.kunlab.scenamatica.commons.utils.ActionMetaUtils;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioException;
import org.kunlab.scenamatica.interfaces.ExceptionHandler;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatcherManager;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.trigger.TriggerManager;

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

    private final TriggerManager triggerManager;
    private final ExceptionHandler exceptionHandler;

    private final Object lock = new Object();
    private final Multimap<Plugin, WatchEntry> actionWatchers;

    public WatcherManagerImpl(@NotNull TriggerManager triggerManager, @NotNull ExceptionHandler exceptionHandler)
    {
        this.triggerManager = triggerManager;
        this.exceptionHandler = exceptionHandler;
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
        if (!(actionExecutor instanceof Expectable))
            throw new IllegalStateException("The action " + ActionMetaUtils.getActionName(actionExecutor) + " is not watchable.");

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

        Expectable expectable = (Expectable) actionExecutor;

        for (Class<? extends Event> eventType : expectable.getAttachingEvents())
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
            this.triggerManager.performTriggerFire(
                    entry.getEngine().getPlugin(),
                    entry.getScenario().getName(),
                    TriggerType.ON_ACTION,
                    entry.getAction().getContext().getInput()
            );
        }
        catch (ScenarioException e)
        {
            this.exceptionHandler.report(e);
        }

    }

    private RegisteredListener registerListener(WatchEntry entry, Class<? extends Event> eventType)
    {
        Action actionExecutor = entry.getAction().getExecutor();
        if (!(actionExecutor instanceof Expectable))
            throw new IllegalStateException("The action " + ActionMetaUtils.getActionName(actionExecutor) + " is not watchable.");

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
        assert actionExecutor instanceof Expectable;
        Expectable expectable = (Expectable) actionExecutor;

        boolean isJumped = this.checkJumped(entry);

        // 引数の解決を試みる。（この状態では、引数の解決が完了していない可能性がある。）
        Throwable resolveError = this.tryResolve(entry);
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

        // けしからん Bukkit が, 対応していないイベントを読んでくるので, 本当に会っているかチェックする。
        boolean isMatched = false;
        Class<? extends Event> evtType = evt.getClass();
        for (Class<? extends Event> eventType : expectable.getAttachingEvents())
        {
            if (eventType.isAssignableFrom(evtType))
            {
                isMatched = true;
                break;
            }
        }

        if (!isMatched)
            return;

        try
        {

            // 引数にマッチしているかどうかをチェックする。
            if (expectable.checkFired(action.getContext(), evt))
                this.onActionFired(entry, isJumped);
        }
        catch (Throwable e)
        {
            entry.getEngine().getListener().onActionError(entry.getAction(), e);
        }
    }

    @Nullable
    private Throwable tryResolve(@NotNull WatchEntry entry)
    {
        try
        {
            entry.getEngine().getExecutor().resolveInputs(entry.getAction());
            return null;
        }
        catch (Throwable e)
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
