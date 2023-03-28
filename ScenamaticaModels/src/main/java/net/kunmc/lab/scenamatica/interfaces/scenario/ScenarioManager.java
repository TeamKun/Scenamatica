package net.kunmc.lab.scenamatica.interfaces.scenario;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public interface ScenarioManager
{
    boolean isRunning();

    TestResult startScenario(@NotNull Plugin plugin, @NotNull String scenarioName);

    void cancel();

    void unloadPluginScenarios(@NotNull Plugin plugin);

    void loadPluginScenarios(@NotNull Plugin plugin);

    void reloadPluginScenarios(@NotNull Plugin plugin);

    ScenarioEngine getCurrentScenario();
}
