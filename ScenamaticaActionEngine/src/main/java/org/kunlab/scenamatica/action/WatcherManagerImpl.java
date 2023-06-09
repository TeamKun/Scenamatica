package org.kunlab.scenamatica.action;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatcherManager;
import org.kunlab.scenamatica.interfaces.action.WatchingEntry;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class WatcherManagerImpl implements WatcherManager
{
    private final ScenamaticaRegistry registry;
    private final Multimap<Plugin, WatchingEntry<?>> actionWatchers;

    public WatcherManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.actionWatchers = ArrayListMultimap.create();
    }

    @Override
    public List<WatchingEntry<?>> registerWatchers(@NotNull Plugin plugin,
                                                   @NotNull ScenarioEngine engine,
                                                   @NotNull ScenarioFileBean scenario,
                                                   @NotNull List<? extends CompiledAction<?>> watchers,
                                                   @NotNull WatchType type)
    {
        List<WatchingEntry<?>> entries = new ArrayList<>();
        for (CompiledAction<?> watcher : watchers)
            entries.add(this.createWatchingEntry(
                            engine,
                            watcher,
                            scenario,
                            plugin,
                            type
                    )
            );

        this.actionWatchers.putAll(plugin, entries);
        return entries;
    }

    @Override
    public <A extends ActionArgument> WatchingEntry<A> registerWatcher(@NotNull ScenarioEngine engine,
                                                                       @NotNull CompiledAction<A> watcher,
                                                                       @NotNull ScenarioFileBean scenario,
                                                                       @NotNull Plugin plugin,
                                                                       @NotNull WatchType type)
    {
        WatchingEntry<A> entry = this.createWatchingEntry(
                engine,
                watcher,
                scenario,
                plugin,
                type
        );
        this.actionWatchers.put(plugin, entry);
        return entry;
    }

    private <A extends ActionArgument> WatchingEntry<A> createWatchingEntry(@NotNull ScenarioEngine engine,
                                                                            @NotNull CompiledAction<A> action,
                                                                            @NotNull ScenarioFileBean scenario,
                                                                            @NotNull Plugin plugin,
                                                                            @NotNull WatchType type)
    {
        Action<A> actionExecutor = action.getExecutor();
        if (!(actionExecutor instanceof Watchable))
            throw new IllegalStateException("The action " + actionExecutor.getName() + " is not watchable.");

        List<Pair<Class<? extends Event>, RegisteredListener>> listeners = new ArrayList<>();
        WatchingEntry<A> watchingEntry = new WatchingEntryImpl<>(
                this,
                engine,
                plugin,
                scenario,
                action,
                type,
                listeners
        );

        Watchable<?> watchable = (Watchable<?>) actionExecutor;

        for (Class<? extends Event> eventType : watchable.getAttachingEvents())
            listeners.add(Pair.of(eventType, watchingEntry.register(eventType)));

        return watchingEntry;
    }

    @Override
    public void unregisterWatchers(@NotNull Plugin plugin)
    {
        if (!this.actionWatchers.containsKey(plugin))
            throw new IllegalStateException("The plugin " + plugin.getName() + " is not registered.");

        Collection<WatchingEntry<?>> entries = this.actionWatchers.get(plugin);
        for (WatchingEntry<?> entry : entries)
            entry.unregister();
        this.actionWatchers.removeAll(plugin);
    }

    @Override
    public void unregisterWatchers(@NotNull Plugin plugin, @NotNull WatchType type)
    {
        if (!this.actionWatchers.containsKey(plugin))
            return;

        List<WatchingEntry<?>> entries = new ArrayList<>(this.actionWatchers.get(plugin));  // ConcurrentModificationException
        for (WatchingEntry<?> entry : entries)
        {
            if (entry.getType() != type)
                continue;

            entry.unregister();
            this.actionWatchers.remove(plugin, entry);
        }
    }

    @Override
    public void unregisterWatcher(@NotNull WatchingEntry<?> entry)
    {
        if (!this.actionWatchers.containsValue(entry))
            throw new IllegalStateException("The entry is not registered.");

        entry.unregister();
        this.actionWatchers.remove(entry.getPlugin(), entry);
    }

    @Override
    public void onActionFired(@NotNull WatchingEntry<?> entry, @NotNull Event event)
    {
        if (!this.actionWatchers.containsValue(entry))
            throw new IllegalStateException("The entry is not registered.");

        if (entry.getType() == WatchType.TRIGGER)
        {
            try
            {
                this.registry.getTriggerManager().performTriggerFire(
                        entry.getPlugin(),
                        entry.getScenario().getName(),
                        TriggerType.ON_ACTION,
                        entry.getAction().getArgument()
                );
            }
            catch (ScenarioException e)
            {
                this.registry.getExceptionHandler().report(e);
            }
        }
        else
        {
            entry.getEngine().getListener().onActionFired(entry, event);
            this.actionWatchers.remove(entry.getPlugin(), entry);
        }
    }
}
