package org.kunlab.scenamatica.action;

import lombok.Value;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.event.Event;
import org.bukkit.plugin.RegisteredListener;
import org.kunlab.scenamatica.enums.WatchType;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatcherManager;
import org.kunlab.scenamatica.interfaces.action.WatchingEntry;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

import java.util.List;

@Value
public class WatchingEntryImpl implements WatchingEntry
{
    WatcherManager manager;
    ScenarioEngine engine;
    ScenarioFileStructure scenario;
    CompiledAction action;
    WatchType type;
    List<Pair<Class<? extends Event>, RegisteredListener>> listeners;

}
