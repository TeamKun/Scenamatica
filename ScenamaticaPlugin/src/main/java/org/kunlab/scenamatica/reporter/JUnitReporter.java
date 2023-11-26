package org.kunlab.scenamatica.reporter;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;
import org.kunlab.scenamatica.results.ScenarioResultWriter;

public class JUnitReporter implements TestReporter
{
    private final ScenarioResultWriter writer;

    public JUnitReporter(ScenarioResultWriter writer)
    {
        this.writer = writer;
    }

    @Override
    public void onTestStart(@NotNull ScenarioEngine engine, @NotNull TriggerStructure trigger)
    {
        this.writer.getLogCapture().startCapture(engine.getTestID());
    }

    @Override
    public void onTestSkipped(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {

    }

    @Override
    public void onActionStart(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {

    }

    @Override
    public void onActionSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action)
    {

    }

    @Override
    public void onWatchingActionExecuted(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action)
    {

    }

    @Override
    public void onActionJumped(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action, @NotNull CompiledAction<?> expected)
    {

    }

    @Override
    public void onActionExecuteFailed(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action, @NotNull Throwable error)
    {

    }

    @Override
    public void onConditionCheckSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {

    }

    @Override
    public void onConditionCheckFailed(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {

    }

    @Override
    public void onTestEnd(@NotNull ScenarioEngine engine, @NotNull ScenarioResult result)
    {
        this.writer.getLogCapture().endCapture();
    }

    @Override
    public void onTestSessionStart(@NotNull ScenarioSession session)
    {

    }

    @Override
    public void onTestSessionEnd(@NotNull ScenarioSession session)
    {
        this.writer.write(session);
        this.writer.getLogCapture().clear();
    }
}
