package net.kunmc.lab.scenamatica.scenario;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.Getter;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScenarioManager
{
    private final ScenamaticaRegistry registry;
    private final ActionManager actionManager;
    private final Multimap<Plugin, ScenarioEngine> engines;
    @Getter
    @Nullable
    private ScenarioEngine running;

    public ScenarioManager(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.actionManager = registry.getActionManager();
        this.engines = ArrayListMultimap.create();
    }

    public boolean isRunning()
    {
        return this.running != null && this.running.getState() != TestState.FINISHED;
    }


}
