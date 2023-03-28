package net.kunmc.lab.scenamatica.trigger;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import net.kunmc.lab.scenamatica.interfaces.trigger.TriggerManager;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

public class TriggerManagerImpl implements TriggerManager
{
    private final ScenamaticaRegistry registry;
    private final Multimap<String, TriggerBean> triggers;  // シナリオ名 / トリガー

    public TriggerManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.triggers = ArrayListMultimap.create();
    }

    @Override
    public void addTrigger(@NotNull String scenario, List<? extends TriggerBean> trigger)
    {
        String scenarioName = scenario.toLowerCase(Locale.ROOT);
        if (this.triggers.containsKey(scenarioName))
            throw new IllegalArgumentException("The scenario " + scenarioName + " is already registered.");

        this.triggers.putAll(scenarioName, trigger);
    }

    @Override
    public void performTriggerFire(@NotNull Plugin plugin,
                                   @NotNull String scenarioName,
                                   @NotNull TriggerType type,
                                   @Nullable TriggerArgument argument)
    {
        String key = scenarioName.toLowerCase(Locale.ROOT);
        if (!this.triggers.containsKey(key))
            throw new IllegalArgumentException("The scenario " + scenarioName + " is not registered.");

        for (TriggerBean trigger : this.triggers.get(key))
        {
            if (trigger.getType() != type)
                continue;

            if (trigger.getArgument() != null && !trigger.getArgument().isSame(argument))
                continue;

            this.registry.getScenarioManager().startScenario(plugin, scenarioName, trigger.getType());
        }
    }
}
