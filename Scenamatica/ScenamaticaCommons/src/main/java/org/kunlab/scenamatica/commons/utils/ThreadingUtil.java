package org.kunlab.scenamatica.commons.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@UtilityClass
public class ThreadingUtil
{
    private static ScenamaticaRegistry registry;

    public static void init(ScenamaticaRegistry registry)
    {
        if (ThreadingUtil.registry != null)
            throw new IllegalStateException("ThreadingUtil has already been initialized.");
        ThreadingUtil.registry = registry;
    }

    private static void ensureInitialized()
    {
        if (registry == null)
            throw new IllegalStateException("ThreadingUtil has not been initialized.");
    }

    @SneakyThrows({BrokenBarrierException.class, InterruptedException.class})
    public static void waitFor(Runnable runnable)
    {
        ensureInitialized();
        CyclicBarrier barrier = new CyclicBarrier(2);
        Runner.run(registry.getPlugin(), () ->
        {
            try
            {
                runnable.run();
                barrier.await();
            }
            catch (InterruptedException | BrokenBarrierException e)
            {
                throw e;  // リスロー
            }
            catch (Exception e)
            {
                registry.getExceptionHandler().report(e);
            }
        });

        barrier.await();
    }

    @SneakyThrows({BrokenBarrierException.class, InterruptedException.class})
    public <T> T waitFor(Supplier<T> supplier)
    {
        ensureInitialized();
        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicReference<T> result = new AtomicReference<>();
        Runner.run(registry.getPlugin(), () ->
        {
            try
            {
                result.set(supplier.get());
                barrier.await();
            }
            catch (InterruptedException | BrokenBarrierException e)
            {
                throw e;  // リスロー
            }
            catch (Exception e)
            {
                registry.getExceptionHandler().report(e);
            }
        });

        barrier.await();
        return result.get();
    }

    @SneakyThrows({BrokenBarrierException.class, InterruptedException.class})
    public <T, E extends Exception> T waitForOrThrow(ThrowableSupplier<T, E> supplier) throws E
    {
        ensureInitialized();
        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicReference<T> result = new AtomicReference<>();
        AtomicReference<E> exception = new AtomicReference<>();
        Runner.run(registry.getPlugin(), () ->
        {
            try
            {
                result.set(supplier.get());
                barrier.await();
            }
            catch (InterruptedException | BrokenBarrierException e)
            {
                throw e;  // リスロー
            }
            catch (Exception e)
            {
                exception.set((E) e);
                barrier.await();
            }
        });

        barrier.await();
        if (exception.get() != null)
            throw exception.get();
        return result.get();
    }

    @FunctionalInterface
    public interface ThrowableSupplier<T, E extends Exception>
    {
        T get() throws E;
    }
}
