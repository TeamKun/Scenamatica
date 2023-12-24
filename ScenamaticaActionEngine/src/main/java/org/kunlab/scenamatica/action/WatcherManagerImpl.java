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
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatcherManager;
import org.kunlab.scenamatica.interfaces.action.WatchingEntry;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class WatcherManagerImpl implements WatcherManager
{
    private final ScenamaticaRegistry registry;
    private final Object lock = new Object();
    private final Multimap<Plugin, WatchingEntry> actionWatchers;

    public WatcherManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.actionWatchers = ArrayListMultimap.create();
    }

    @Override
    public List<WatchingEntry> registerWatchers(@NotNull Plugin plugin,
                                                @NotNull ScenarioEngine engine,
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
                            plugin,
                            type
                    )
            );

        synchronized (this.lock)
        {
            this.actionWatchers.putAll(plugin, entries);
        }

        return entries;
    }

    @Override
    public WatchingEntry registerWatcher(@NotNull ScenarioEngine engine,
                                         @NotNull CompiledAction watcher,
                                         @NotNull ScenarioFileStructure scenario,
                                         @NotNull Plugin plugin,
                                         @NotNull WatchType type)
    {
        WatchingEntry entry = this.createWatchingEntry(
                engine,
                watcher,
                scenario,
                plugin,
                type
        );

        synchronized (this.lock)
        {
            this.actionWatchers.put(plugin, entry);
        }

        return entry;
    }

    private WatchingEntry createWatchingEntry(@NotNull ScenarioEngine engine,
                                              @NotNull CompiledAction action,
                                              @NotNull ScenarioFileStructure scenario,
                                              @NotNull Plugin plugin,
                                              @NotNull WatchType type)
    {
        Action actionExecutor = action.getExecutor();
        if (!(actionExecutor instanceof Watchable))
            throw new IllegalStateException("The action " + actionExecutor.getName() + " is not watchable.");

        List<Pair<Class<? extends Event>, RegisteredListener>> listeners = new ArrayList<>();
        WatchingEntry watchingEntry = new WatchingEntryImpl(
                this,
                engine,
                plugin,
                scenario,
                action,
                type,
                listeners
        );

        Watchable watchable = (Watchable) actionExecutor;

        for (Class<? extends Event> eventType : watchable.getAttachingEvents())
            listeners.add(Pair.of(eventType, watchingEntry.register(eventType)));

        return watchingEntry;
    }

    @Override
    public void unregisterWatchers(@NotNull Plugin plugin)
    {
        if (!this.actionWatchers.containsKey(plugin))
            throw new IllegalStateException("The plugin " + plugin.getName() + " is not registered.");

        synchronized (this.lock)
        {
            Collection<WatchingEntry> entries = this.actionWatchers.get(plugin);
            for (WatchingEntry entry : entries)
                entry.unregister();
            this.actionWatchers.removeAll(plugin);
        }

    }

    @Override
    public void unregisterWatchers(@NotNull Plugin plugin, @NotNull WatchType type)
    {
        synchronized (this.lock)
        {
            if (!this.actionWatchers.containsKey(plugin))
                return;

            Iterator<WatchingEntry> iterator = this.actionWatchers.get(plugin).iterator();
            while (iterator.hasNext())
            {
                WatchingEntry entry = iterator.next();
                if (entry.getType() == type)
                {
                    entry.unregister();
                    iterator.remove();
                }
            }
        }
    }

    @Override
    public void unregisterWatcher(@NotNull WatchingEntry entry)
    {
        synchronized (this.lock)
        {
            if (!this.actionWatchers.containsValue(entry))
                throw new IllegalStateException("The entry is not registered.");

            entry.unregister();
            this.actionWatchers.remove(entry.getPlugin(), entry);
        }
    }

    @Override
    public void onActionFired(@NotNull WatchingEntry entry, @NotNull Event event)
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
            synchronized (this.lock)
            {
                this.actionWatchers.remove(entry.getPlugin(), entry);
            }
        }
    }
}
