package org.kunlab.scenamatica.action;

import lombok.Value;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.kunlab.scenamatica.action.utils.EventListenerUtils;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatcherManager;
import org.kunlab.scenamatica.interfaces.action.WatchingEntry;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileBean;

import java.util.List;

@Value
public class WatchingEntryImpl<A extends ActionArgument> implements WatchingEntry<A>
{
    WatcherManager manager;
    ScenarioEngine engine;
    Plugin plugin;
    ScenarioFileBean scenario;
    CompiledAction<A> action;
    WatchType type;
    List<Pair<Class<? extends Event>, RegisteredListener>> listeners;

    @Override
    public RegisteredListener register(Class<? extends Event> eventType)
    {
        Action<A> actionExecutor = this.action.getExecutor();
        if (!(actionExecutor instanceof Watchable))
            throw new IllegalStateException("The action " + actionExecutor.getName() + " is not watchable.");
        //noinspection unchecked
        Watchable<A> watchable = (Watchable<A>) actionExecutor;

        Listener dummyListener = new Listener()
        {
            private final String name = "DummyListener for " + WatchingEntryImpl.this.action.getClass().getName();

            public String getName()
            {
                return this.name;
            }
        };

        EventExecutor executor = (listener1, event) -> {
            if (watchable.isFired(this.action.getArgument(), WatchingEntryImpl.this.engine, event))
            {
                this.manager.onActionFired(WatchingEntryImpl.this, event);
                if (this.type == WatchType.SCENARIO)
                    WatchingEntryImpl.this.unregister();  // シナリオのアクションは一度だけ実行するようにする。(トリガは複数)
            }
        };

        RegisteredListener registeredListener = new RegisteredListener(
                dummyListener,
                executor,
                EventPriority.LOWEST,  // ほかのイベントハンドラによる改変を許可。
                this.plugin,
                true  // キャンセルされたら反応しないようにする。
        );

        EventListenerUtils.getListeners(eventType).register(registeredListener);

        return registeredListener;
    }

    @Override
    public void unregister()
    {
        for (Pair<Class<? extends Event>, RegisteredListener> listenerPair : this.getListeners())
            EventListenerUtils.getListeners(listenerPair.getLeft()).unregister(listenerPair.getRight());
    }
}
