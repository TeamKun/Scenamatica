package net.kunmc.lab.scenamatica.utils;

import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CommandUtils
{
    @NotNull
    public static List<String> getScenariosByTrigger(ScenamaticaRegistry registry, TriggerType type)
    {
        return Arrays.stream(Bukkit.getPluginManager().getPlugins()).parallel()
                .filter(plugin -> {
                    Map<String, ScenarioFileBean> scenarios = registry.getScenarioFileManager().getPluginScenarios(plugin);

                    // MANUAL_DISPATCH トリガを持っているか確認する、
                    return scenarios != null && scenarios.values().stream().parallel()
                            .anyMatch(scenario -> scenario.getTriggers().stream().parallel()
                                    .anyMatch(trigger -> trigger.getType() == type));
                })
                .map(Plugin::getName)
                .collect(Collectors.toList());
    }
}