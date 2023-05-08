package net.kunmc.lab.scenamatica.scenario;

import lombok.Getter;
import net.kunmc.lab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import net.kunmc.lab.scenamatica.interfaces.scenario.QueuedScenario;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResult;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@Getter
public class QueuedScenarioImpl implements QueuedScenario
{
    private final ScenarioManagerImpl manager;
    private final long queuedAt;
    @NotNull
    private final ScenarioEngine engine;
    @NotNull
    private final TriggerBean trigger;
    private final @Nullable Consumer<? super ScenarioResult> callback;

    private long startedAt;
    private boolean running;
    private ScenarioResult result;
    private long finishedAt;

    public QueuedScenarioImpl(
            @NotNull ScenarioManagerImpl manager, @NotNull ScenarioEngine engine, @NotNull TriggerBean trigger,
            @Nullable Consumer<? super ScenarioResult> callback)
    {
        this.manager = manager;
        this.queuedAt = System.currentTimeMillis();
        this.engine = engine;
        this.trigger = trigger;
        this.callback = callback;
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
            this.callback.accept(result);
    }

    @Override
    public ScenarioResult run() throws TriggerNotFoundException
    {
        this.onStart();
        ScenarioResult result = this.manager.runScenario(this.engine, this.trigger);
        this.onFinished(result);

        if (this.callback != null)
            try
            {
                this.callback.accept(result);
            }
            catch (Exception e)
            {
                this.manager.getRegistry().getExceptionHandler().report(e);
            }

        return result;
    }

    private void sureRunning()
    {
        if (this.running)
            throw new IllegalStateException("Scenario is still running.");
    }

    private void sureNotRunning()
    {
        if (!this.running)
            throw new IllegalStateException("Scenario is not running.");
    }

    public long getFinishedAt()
    {
        this.sureNotRunning();

        return this.finishedAt;
    }

    public long getStartedAt()
    {
        this.sureRunning();

        return this.startedAt;
    }

    public long getTakenTime()
    {
        this.sureNotRunning();

        return this.finishedAt - this.startedAt;
    }

}
