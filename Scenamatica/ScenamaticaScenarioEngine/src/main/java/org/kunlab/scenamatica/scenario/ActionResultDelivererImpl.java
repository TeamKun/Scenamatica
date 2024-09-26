package org.kunlab.scenamatica.scenario;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioState;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.scenario.ActionResultDeliverer;

import java.util.ArrayDeque;

public class ActionResultDelivererImpl implements ActionResultDeliverer
{
    private final Object lock = new Object();
    private final ArrayDeque<ActionResult> results;  // 受け渡し用

    @Getter
    private boolean waiting;  // wait されているかどうか。タイムアウト制御用。
    private RuntimeException caughtException;
    private long waitTimeout;
    private long elapsedTick;

    public ActionResultDelivererImpl()
    {
        this.results = new ArrayDeque<>();
    }

    @Override
    public synchronized void setResult(ActionResult result)
    {
        synchronized (this.lock)
        {
            this.results.add(result);

            this.waitTimeout = -1L;
            this.elapsedTick = 0;

            this.lock.notifyAll();
        }
    }

    @Override
    @NotNull
    public ActionResult waitForResult(long timeout, @NotNull ScenarioState state)
    {
        if (timeout > 0)
            this.waitTimeout = timeout;

        synchronized (this.lock)
        {
            this.waiting = true;  // タイムアウト制御用
            this.elapsedTick = 0;

            if (!this.results.isEmpty())
                return this.results.pop();

            try
            {
                this.lock.wait();
            }
            catch (InterruptedException e)
            {
                throw new IllegalStateException(e);
            }

            this.waiting = false;
            if (this.caughtException != null)
                throw this.caughtException;

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
            this.timedout();
        }
    }

    @Override
    public void timedout()
    {
        this.setCaughtException(new ScenarioWaitTimedOutException());
    }

    public void setCaughtException(Throwable caughtException)
    {
        synchronized (this.lock)
        {
            if (!this.waiting)
                return;

            if (caughtException instanceof RuntimeException)
                this.caughtException = (RuntimeException) caughtException;
            else
                this.caughtException = new RuntimeException(caughtException);

            this.lock.notifyAll();
        }
    }

    @Override
    public void kill()
    {
        synchronized (this.lock)
        {
            this.waiting = false;
            this.lock.notifyAll();
        }
    }
}
