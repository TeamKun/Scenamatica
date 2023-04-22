package net.kunmc.lab.scenamatica.scenario;

import net.kunmc.lab.scenamatica.enums.TestResultCause;
import net.kunmc.lab.scenamatica.enums.TestState;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResultDeliverer;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestResult;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayDeque;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ScenarioResultDelivererImpl implements ScenarioResultDeliverer
{
    private final Object lock = new Object();
    private final CyclicBarrier barrier;
    private final ScenamaticaRegistry registry;
    private final UUID testID;
    private final long startedAt;
    private final ArrayDeque<TestResult> results;  // 受け渡し用

    private boolean killed;  // シャットダウンされたら true

    private TestState state;  // wait されたら入る。終わったら消す
    private boolean waiting;  // wait されているかどうか。タイムアウト制御用。
    private long waitTimeout;
    private long elapsedTick;

    public ScenarioResultDelivererImpl(@NotNull ScenamaticaRegistry registry,
                                       @NotNull UUID testID,
                                       long startedAt)

    {
        this.barrier = new CyclicBarrier(2);
        this.registry = registry;
        this.testID = testID;
        this.startedAt = startedAt;

        this.results = new ArrayDeque<>();
        this.killed = false;
    }

    @Override
    public synchronized void setResult(TestResult result)
    {
        synchronized (this.lock)
        {
            this.results.add(result);

            this.state = null;
            this.waitTimeout = -1L;
            this.elapsedTick = 0;
        }

        try
        {
            this.barrier.await();
        }
        catch (InterruptedException e)
        {
            this.registry.getExceptionHandler().report(e);
        }
        catch (BrokenBarrierException e)
        {
            if (!this.killed)
                this.registry.getExceptionHandler().report(e);
        }
    }

    @Override
    @NotNull
    public TestResult waitResult(long timeout, @NotNull TestState state)
    {
        if (timeout > 0)
            this.waitTimeout = timeout;

        this.state = state;
        this.waiting = true;  // タイムアウト制御用
        this.elapsedTick = 0;

        try
        {
            this.barrier.await();
        }
        catch (InterruptedException e)
        {
            this.registry.getExceptionHandler().report(e);
            return new TestResultImpl(
                    this.testID,
                    state,
                    TestResultCause.INTERNAL_ERROR,
                    this.startedAt
            );
        }
        catch (BrokenBarrierException e)
        {
            if (!this.killed)
                this.registry.getExceptionHandler().report(e);
            return new TestResultImpl(
                    this.testID,
                    state,
                    TestResultCause.CANCELLED,
                    this.startedAt
            );
        }

        synchronized (this.lock)
        {
            this.waiting = false;
            return this.results.pop();
        }
    }

    @Override
    public void onTick()
    {
        if (!this.waiting || this.waitTimeout <= 0)
            return;

        this.elapsedTick++;

        if (this.elapsedTick >= this.waitTimeout)
        {
            this.waitTimeout = -1L;
            this.setResult(new TestResultImpl(
                    this.testID,
                    this.state,
                    TestResultCause.SCENARIO_TIMED_OUT,
                    this.startedAt
            ));
        }
    }

    @Override
    public void kill()
    {
        this.killed = true;
        this.barrier.reset();
        try
        {
            this.barrier.await(1, TimeUnit.MILLISECONDS);
        }
        catch (InterruptedException | BrokenBarrierException | TimeoutException ignored)
        {
            // BrokenBarrierException を上で起こさせるだけなので, 握りつぶす
        }
    }
}
