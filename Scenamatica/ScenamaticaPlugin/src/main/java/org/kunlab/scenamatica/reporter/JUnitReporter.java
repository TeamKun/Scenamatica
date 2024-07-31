package org.kunlab.scenamatica.reporter;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;
import org.kunlab.scenamatica.results.ScenarioResultWriter;

public class JUnitReporter extends AbstractTestReporter
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
    public void onTestEnd(@NotNull ScenarioEngine engine, @NotNull ScenarioResult result)
    {
        this.writer.getLogCapture().endCapture();
    }

    @Override
    public void onTestSessionEnd(@NotNull ScenarioSession session)
    {
        this.writer.write(session);
        this.writer.getLogCapture().clear();
    }
}
