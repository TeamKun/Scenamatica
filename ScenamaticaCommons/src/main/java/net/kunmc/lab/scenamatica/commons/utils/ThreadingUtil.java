package net.kunmc.lab.scenamatica.commons.utils;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import net.kunmc.lab.peyangpaperutils.lib.utils.Runner;
import org.bukkit.plugin.Plugin;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@UtilityClass
public class ThreadingUtil
{
    @SneakyThrows({BrokenBarrierException.class, InterruptedException.class})
    public static void waitFor(Plugin plugin, Runnable runnable)
    {
        CyclicBarrier barrier = new CyclicBarrier(2);
        Runner.run(plugin, () ->
        {
            runnable.run();
            barrier.await();
        });

        barrier.await();
    }

    @SneakyThrows({BrokenBarrierException.class, InterruptedException.class})
    public <T> T waitFor(Plugin plugin, Supplier<T> supplier)
    {
        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicReference<T> result = new AtomicReference<>();
        Runner.run(plugin, () ->
        {
            result.set(supplier.get());
            barrier.await();
        });

        barrier.await();
        return result.get();
    }
}
