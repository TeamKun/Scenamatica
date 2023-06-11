package net.kunmc.lab.scenamatica.reporter;

import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResult;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioSession;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestReporter;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ReportersBridge implements TestReporter
{
    private final List<? extends TestReporter> reporters;

    public ReportersBridge(List<? extends TestReporter> reporters)
    {
        this.reporters = reporters;
    }

    @Override
    public void onTestStart(@NotNull ScenarioEngine engine, @NotNull TriggerBean trigger)
    {
        this.reporters.forEach(reporter -> reporter.onTestStart(engine, trigger));
    }

    @Override
    public void onTestSkipped(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        this.reporters.forEach(reporter -> reporter.onTestSkipped(engine, action));
    }

    @Override
    public void onActionStart(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        this.reporters.forEach(reporter -> reporter.onActionStart(engine, action));
    }

    @Override
    public void onActionSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action)
    {
        this.reporters.forEach(reporter -> reporter.onActionSuccess(engine, action));
    }

    @Override
    public void onWatchingActionExecuted(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action)
    {
        this.reporters.forEach(reporter -> reporter.onWatchingActionExecuted(engine, action));
    }

    @Override
    public void onActionJumped(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action, @NotNull CompiledAction<?> expected)
    {
        this.reporters.forEach(reporter -> reporter.onActionJumped(engine, action, expected));
    }

    @Override
    public void onActionExecuteFailed(@NotNull ScenarioEngine engine, @NotNull CompiledAction<?> action, @NotNull Throwable error)
    {
        this.reporters.forEach(reporter -> reporter.onActionExecuteFailed(engine, action, error));
    }

    @Override
    public void onConditionCheckSuccess(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        this.reporters.forEach(reporter -> reporter.onConditionCheckSuccess(engine, action));
    }

    @Override
    public void onConditionCheckFailed(@NotNull ScenarioEngine engine, @NotNull CompiledScenarioAction<?> action)
    {
        this.reporters.forEach(reporter -> reporter.onConditionCheckFailed(engine, action));
    }

    @Override
    public void onTestEnd(@NotNull ScenarioEngine engine, @NotNull ScenarioResult result)
    {
        this.reporters.forEach(reporter -> reporter.onTestEnd(engine, result));
    }

    @Override
    public void onTestSessionStart(@NotNull ScenarioSession session)
    {
        this.reporters.forEach(reporter -> reporter.onTestSessionStart(session));
    }

    @Override
    public void onTestSessionEnd(@NotNull ScenarioSession session)
    {
        this.reporters.forEach(reporter -> reporter.onTestSessionEnd(session));
    }
}
