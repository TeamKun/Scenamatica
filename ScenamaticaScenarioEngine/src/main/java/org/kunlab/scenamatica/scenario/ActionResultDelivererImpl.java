package org.kunlab.scenamatica.scenario;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioState;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.scenario.ActionResultDeliverer;

import java.util.ArrayDeque;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ActionResultDelivererImpl implements ActionResultDeliverer
{
    private final Object lock = new Object();
    private final CyclicBarrier barrier;
    private final ArrayDeque<ActionResult> results;  // 受け渡し用

    @Getter
    private boolean waiting;  // wait されているかどうか。タイムアウト制御用。
    private RuntimeException exceptionCaught;
    private long waitTimeout;
    private long elapsedTick;

    public ActionResultDelivererImpl()
    {
        this.barrier = new CyclicBarrier(2);

        this.results = new ArrayDeque<>();
    }

    @Override
    public synchronized void setResult(ActionResult result)
    {
        synchronized (this.lock)
        {
            if (this.barrier.getNumberWaiting() > 1)
                return;

            this.results.add(result);

            this.waitTimeout = -1L;
            this.elapsedTick = 0;
        }

        this.await();
    }

    @Override
    @NotNull
    public ActionResult waitForResult(long timeout, @NotNull ScenarioState state)
    {
        if (timeout > 0)
            this.waitTimeout = timeout;

        this.waiting = true;  // タイムアウト制御用
        this.elapsedTick = 0;
        this.await();

        synchronized (this.lock)
        {
            this.waiting = false;
            if (this.exceptionCaught != null)
                throw this.exceptionCaught;

            return this.results.pop();
        }
    }

    private void await()
    {
        try
        {
            this.barrier.await();
        }
        catch (InterruptedException | BrokenBarrierException e)
        {
            throw new IllegalStateException(e);
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
            this.timedout();
        }
    }

    @Override
    public void timedout()
    {
        this.setExceptionCaught(new ScenarioWaitTimedOutException());
    }

    @Override
    public void setExceptionCaught(Throwable exceptionCaught)
    {
        if (!this.waiting)
            return;

        if (exceptionCaught instanceof RuntimeException)
            this.exceptionCaught = (RuntimeException) exceptionCaught;
        else
            this.exceptionCaught = new RuntimeException(exceptionCaught);
        this.await();
    }

    @Override
    public void kill()
    {
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
