package net.kunmc.lab.scenamatica.action;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.enums.WatchType;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.WatcherManager;
import net.kunmc.lab.scenamatica.interfaces.action.WatchingEntry;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
                                                   @NotNull ScenarioFileBean scenario,
                                                   @NotNull List<? extends Pair<Action<?>, ActionArgument>> watchers,
                                                   @NotNull WatchType type)
    {
        List<WatchingEntry<?>> entries = new ArrayList<>();
        for (Pair<Action<?>, ActionArgument> watcher : watchers)
            //noinspection unchecked
            entries.add(this.createWatchingEntry(
                            (Action<ActionArgument>) watcher.getLeft(),
                            watcher.getRight(),
                            scenario,
                            plugin,
                            type
                    )
            );

        this.actionWatchers.putAll(plugin, entries);
        return entries;
    }

    @Override
    public <A extends ActionArgument> WatchingEntry<A> registerWatcher(@NotNull Action<A> watcher,
                                                                       @Nullable A argument,
                                                                       @NotNull ScenarioFileBean scenario,
                                                                       @NotNull Plugin plugin,
                                                                       @NotNull WatchType type)
    {
        WatchingEntry<A> entry = this.createWatchingEntry(watcher, argument, scenario, plugin, type);
        this.actionWatchers.put(plugin, entry);
        return entry;
    }

    private <A extends ActionArgument> WatchingEntry<A> createWatchingEntry(@NotNull Action<A> watcher,
                                                                            @Nullable A argument,
                                                                            @NotNull ScenarioFileBean scenario,
                                                                            @NotNull Plugin plugin,
                                                                            @NotNull WatchType type)
    {
        List<Pair<Class<? extends Event>, RegisteredListener>> listeners = new ArrayList<>();
        WatchingEntryImpl<A> watchingEntry = new WatchingEntryImpl<>(
                this,
                plugin,
                scenario,
                watcher,
                argument,
                type,
                listeners
        );
        for (Class<? extends Event> eventType : watcher.getAttachingEvents())
            listeners.add(Pair.of(eventType, watchingEntry.register(watcher, eventType)));

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
    public void onActionFired(@NotNull WatchingEntry<?> entry)
    {
        if (!this.actionWatchers.containsValue(entry))
            throw new IllegalStateException("The entry is not registered.");

        // アクションが「シナリオ」タイプだったら, そのアクションを登録解除する。(「トリガ」は次も行われる可能性がある。)
        if (entry.getType() == WatchType.SCENARIO)
        {
            this.unregisterWatchers(entry.getPlugin());
            this.registry.getTriggerManager().performTriggerFire(
                    entry.getScenario().getName(),
                    TriggerType.ON_ACTION,
                    entry.getArgument()
            );
        }
        else
        {
            // TODO: アクションの実行をキューに入れる
        }
    }
}
