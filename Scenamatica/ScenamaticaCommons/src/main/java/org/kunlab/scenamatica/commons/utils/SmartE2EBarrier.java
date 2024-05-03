package org.kunlab.scenamatica.commons.utils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class SmartE2EBarrier
{
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private boolean isReleased;
    private int parties;

    public SmartE2EBarrier(int parties)
    {
        this.parties = parties;
        this.isReleased = false;
    }

    public SmartE2EBarrier()
    {
        this(2);
    }

    public void await(long timeoutMillis) throws InterruptedException
    {
        this.lock.lock();
        try {
            if (this.isReleased)
                return;

            if (this.parties == 1)
            {
                this.isReleased = true;
                this.condition.signalAll();
                return;
            }

            this.parties--;
            if (timeoutMillis <= 0)
                this.condition.await();
            else
                this.condition.await(timeoutMillis, TimeUnit.MILLISECONDS);
        }
        finally
        {
            this.lock.unlock();
        }
    }

    public void await() throws InterruptedException
    {
        this.await(0);
    }

    public void release()
    {
        this.lock.lock();
        try
        {
            this.isReleased = true;
            this.condition.signalAll();
        }
        finally
        {
            this.lock.unlock();
        }
    }

    public void reset()
    {
        this.lock.lock();
        try
        {
            this.isReleased = false;
            this.parties = 0;
        }
        finally
        {
            this.lock.unlock();
        }
    }
}
