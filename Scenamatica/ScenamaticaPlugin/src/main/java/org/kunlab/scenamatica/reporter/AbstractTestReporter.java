package org.kunlab.scenamatica.reporter;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;

public abstract class AbstractTestReporter implements TestReporter
{

    @Override
    public void onTestStart(@NotNull ScenarioEngine engine, @NotNull TriggerStructure trigger)
    {

    }

    @Override
    public void onTestSkipped(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction action)
    {

    }

    @Override
    public void onActionStart(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction action)
    {

    }

    @Override
    public void onActionSuccess(@NotNull ScenarioEngine engine, @NotNull ActionResult result)
    {

    }

    @Override
    public void onWatchingActionExecuted(@NotNull ScenarioEngine engine, @NotNull ActionResult result)
    {

    }

    @Override
    public void onActionJumped(@NotNull ScenarioEngine engine, @NotNull ActionResult result, @NotNull CompiledAction expected)
    {

    }

    @Override
    public void onActionExecuteFailed(@NotNull ScenarioEngine engine, @NotNull CompiledAction action, @NotNull Throwable error)
    {

    }

    @Override
    public void onConditionCheckSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction action)
    {

    }

    @Override
    public void onConditionCheckFailed(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction action)
    {

    }

    @Override
    public void onTestEnd(@NotNull ScenarioEngine engine, @NotNull ScenarioResult result)
    {

    }

    @Override
    public void onTestSessionStart(@NotNull ScenarioSession session)
    {

    }

    @Override
    public void onTestSessionEnd(@NotNull ScenarioSession session)
    {

    }
}
