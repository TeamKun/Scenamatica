package org.kunlab.scenamatica.scenario;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.kunlab.scenamatica.interfaces.scenario.QueuedScenario;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;

import java.util.function.Consumer;

@Getter
public class QueuedScenarioImpl implements QueuedScenario
{
    private final ScenarioManagerImpl manager;
    private final long queuedAt;
    @NotNull
    private final ScenarioEngine engine;
    @NotNull
    private final TriggerStructure trigger;
    private final @Nullable Consumer<? super ScenarioResult> callback;
    private final int maxAttemptCount;

    private long startedAt;
    private boolean running;
    private ScenarioResult result;
    private long finishedAt;
    private int attemptCount;

    public QueuedScenarioImpl(
            @NotNull ScenarioManagerImpl manager, @NotNull ScenarioEngine engine, @NotNull TriggerStructure trigger,
            @Nullable Consumer<? super ScenarioResult> callback,
            int maxAttemptCount)
    {
        this.manager = manager;
        this.queuedAt = System.currentTimeMillis();
        this.engine = engine;
        this.trigger = trigger;
        this.callback = callback;
        this.maxAttemptCount = maxAttemptCount;

        this.startedAt = -1;
        this.running = false;
        this.result = null;
        this.finishedAt = -1;
        this.attemptCount = 1;
    }

    /* non-public */ void onStart()
    {
        this.startedAt = System.currentTimeMillis();
        this.running = true;
    }

    /* non-public */ void onFinished(ScenarioResult result)
    {
        this.running = false;
        this.result = result;
        this.finishedAt = System.currentTimeMillis();
        if (this.callback != null)
            try
            {
                this.callback.accept(result);
            }
            catch (Exception e)
            {
                this.manager.getRegistry().getExceptionHandler().report(e);
            }

    }

    @Override
    public ScenarioResult run() throws TriggerNotFoundException
    {
        this.onStart();
        ScenarioResult result = this.manager.runScenario(this.engine, this.trigger, this.attemptCount);
        this.onFinished(result);

        return result;
    }

    private void ensureNotRunning()
    {
        if (this.running)
            throw new IllegalStateException("Scenario is still running.");
    }

    private void ensureRunning()
    {
        if (!this.running)
            throw new IllegalStateException("Scenario is not running.");
    }

    public long getFinishedAt()
    {
        this.ensureRunning();

        return this.finishedAt;
    }

    public long getStartedAt()
    {
        this.ensureNotRunning();

        return this.startedAt;
    }

    public long getTakenTime()
    {
        this.ensureRunning();

        return this.finishedAt - this.startedAt;
    }

    @Override
    public void resetForRetry()
    {
        this.ensureNotRunning();

        this.startedAt = -1;
        this.running = false;
        this.result = null;
        this.finishedAt = -1;

        this.attemptCount++;
    }

}
