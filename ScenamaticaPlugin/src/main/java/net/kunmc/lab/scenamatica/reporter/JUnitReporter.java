package net.kunmc.lab.scenamatica.reporter;

import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResult;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioSession;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestReporter;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import net.kunmc.lab.scenamatica.results.ScenarioResultWriter;
import org.jetbrains.annotations.NotNull;

public class JUnitReporter implements TestReporter
{
    private final ScenarioResultWriter writer;

    public JUnitReporter(ScenarioResultWriter writer)
    {
        this.writer = writer;
    }

    @Override
    public void onTestStart(@NotNull ScenarioEngine engine, @NotNull TriggerBean trigger)
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
