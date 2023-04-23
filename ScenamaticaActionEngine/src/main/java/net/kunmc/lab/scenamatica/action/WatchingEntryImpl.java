package net.kunmc.lab.scenamatica.action;

import lombok.Value;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import net.kunmc.lab.scenamatica.action.utils.EventListenerUtils;
import net.kunmc.lab.scenamatica.enums.WatchType;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.action.WatcherManager;
import net.kunmc.lab.scenamatica.interfaces.action.WatchingEntry;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;

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
        //noinspection unused
        Listener dummyListener = new Listener()
        {
            private final String name = "DummyListener for " + WatchingEntryImpl.this.action.getClass().getName();
        };

        EventExecutor executor = (listener1, event) -> {
            if (this.action.getExecutor().isFired(this.action.getArgument(), WatchingEntryImpl.this.engine, event))
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
