package net.kunmc.lab.scenamatica.utils;

import net.kunmc.lab.scenamatica.enums.*;
import net.kunmc.lab.scenamatica.interfaces.*;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.*;
import org.bukkit.*;
import org.bukkit.plugin.*;
import org.jetbrains.annotations.*;

import java.util.*;
import java.util.stream.*;

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
