package org.kunlab.scenamatica.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandUtils
{
    @NotNull
    public static List<String> getScenariosByTrigger(ScenamaticaRegistry registry, TriggerType type)
    {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins())
                .filter(plugin -> {
                    Map<String, ScenarioFileStructure> scenarios = registry.getScenarioFileManager().getPluginScenarios(plugin);

                    // MANUAL_DISPATCH トリガを持っているか確認する、
                    return scenarios != null && scenarios.values().stream()
                            .anyMatch(scenario -> scenario.getTriggers().stream()
                                    .anyMatch(trigger -> trigger.getType() == type));
                })
                .map(Plugin::getName)
                .collect(Collectors.toList());
    }
}
