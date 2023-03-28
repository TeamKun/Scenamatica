package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.enums.TestState;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.bukkit.plugin.Plugin;

import java.util.List;
import java.util.UUID;

public interface ScenarioEngine
{
    TestResult start(TriggerBean trigger);

    void cancel();

    Plugin getPlugin();

    ScenarioFileBean getScenario();

    ScenarioActionListener getListener();

    boolean isRunning();

    TriggerBean getRanBy();

    UUID getTestID();

    long getStartedAt();

    String getLogPrefix();

    boolean isAutoRun();

    TestState getState();

    CompiledScenarioAction<?> getCurrentScenario();

    ScenarioResultDelivererImpl getDeliverer();

    List<CompiledScenarioAction<?>> getActions();

    List<CompiledTriggerAction> getTriggerActions();
}
