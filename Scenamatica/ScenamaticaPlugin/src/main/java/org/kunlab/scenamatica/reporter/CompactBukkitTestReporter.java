package org.kunlab.scenamatica.reporter;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

public class CompactBukkitTestReporter extends BukkitTestReporter
{
    @Override
    public void onTestEnd(@NotNull ScenarioEngine engine, @NotNull ScenarioResult result)
    {
        ScenarioFileStructure scenario = engine.getScenario();

        this.terminals.forEach(t -> this.printTestSummary(t, scenario, result));
    }

    @Override
    public void onActionStart(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction action)
    {
        // 握りつぶす
    }

    @Override
    public void onActionSuccess(@NotNull ScenarioEngine engine, @NotNull ActionResult result)
    {
        // 握りつぶす
    }

    @Override
    public void onWatchingActionExecuted(@NotNull ScenarioEngine engine, @NotNull ActionResult result)
    {
        // 握りつぶす
    }

    @Override
    public void onConditionCheckSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction action)
    {
        // 握りつぶす
    }
}
