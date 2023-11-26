package org.kunlab.scenamatica.reporter;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class ReportersBridge implements TestReporter
{
    private final LinkedList<? extends TestReporter> reporters;

    public ReportersBridge(List<? extends TestReporter> reporters)
    {
        this.reporters = new LinkedList<>(reporters);
    }

    @Override
    public void onTestStart(@NotNull ScenarioEngine engine, @NotNull TriggerStructure trigger)
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
        Iterator<? extends TestReporter> iterator = this.reporters.descendingIterator();
        while (iterator.hasNext())
        {
            TestReporter reporter = iterator.next();
            reporter.onTestEnd(engine, result);
        }
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
