package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.enums.TriggerType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface ScenarioManager
{
    boolean isRunning();

    TestResult startScenario(@NotNull Plugin plugin, @NotNull String scenarioName);

    TestResult startScenario(@NotNull Plugin plugin, @NotNull String scenarioName, @NotNull TriggerType triggertype);

    void cancel();

    void unloadPluginScenarios(@NotNull Plugin plugin);

    void loadPluginScenarios(@NotNull Plugin plugin);

    void reloadPluginScenarios(@NotNull Plugin plugin);

    ScenarioEngine getCurrentScenario();

    void shutdown();
}
