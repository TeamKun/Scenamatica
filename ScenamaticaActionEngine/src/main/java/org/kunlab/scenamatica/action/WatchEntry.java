package org.kunlab.scenamatica.action;

import lombok.Data;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredListener;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatcherManager;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

import java.util.List;

@Data
public class WatchEntry
{
    private final WatcherManager manager;
    private final ScenarioEngine engine;
    private final ScenarioFileStructure scenario;
    private final int idx;
    private final CompiledAction action;
    private final WatchType type;
    private final List<Pair<Class<? extends Event>, RegisteredListener>> listeners;

    private boolean enabled;

    public boolean hasListenerFor(Class<? extends Event> event)
    {
        for (Pair<Class<? extends Event>, RegisteredListener> pair : this.listeners)
            if (pair.getLeft() == event)  // Class は参照比較できる
                return true;

        return false;
    }
}
