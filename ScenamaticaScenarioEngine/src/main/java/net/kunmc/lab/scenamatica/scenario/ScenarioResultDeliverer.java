package net.kunmc.lab.scenamatica.scenario;

import net.kunmc.lab.scenamatica.enums.TestResultCause;
import net.kunmc.lab.scenamatica.enums.TestState;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResultDelivererImpl;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestResult;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class ScenarioResultDeliverer implements ScenarioResultDelivererImpl
{
    private final CyclicBarrier barrier;
    private final ScenamaticaRegistry registry;
    private final UUID testID;
    private final long startedAt;

    private TestResult result;
    private boolean killed;

    public ScenarioResultDeliverer(@NotNull ScenamaticaRegistry registry,
                                   @NotNull UUID testID,
                                   long startedAt)

    {
        this.barrier = new CyclicBarrier(2);
        this.registry = registry;
        this.testID = testID;
        this.startedAt = startedAt;

        this.result = null;
        this.killed = false;
    }

    @Override
    public void setResult(TestResult result)
    {
        this.result = result;
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
    public TestResult waitResult(@NotNull TestState state)
    {
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
                    "An internal error occurred while waiting for the result: " + e.getMessage(),
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
                    "An internal error occurred while waiting for the result: " + e.getMessage(),
                    this.startedAt
            );
        }

        return this.result;
    }

    @Override
    public void kill()
    {
        this.killed = true;
        this.barrier.reset();
    }
}
