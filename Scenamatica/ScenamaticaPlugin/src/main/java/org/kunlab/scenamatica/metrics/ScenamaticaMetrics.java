package org.kunlab.scenamatica.metrics;

import lombok.SneakyThrows;
import org.bstats.MetricsBase;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bstats.charts.SingleLineChart;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.Scenamatica;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioSession;
import org.kunlab.scenamatica.reporter.AbstractTestReporter;
import org.kunlab.scenamatica.reporter.ReportersBridge;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ScenamaticaMetrics extends Metrics
{
    private static final int SCENAMATICA_PLUGIN_ID = 24256;
    private static final Field fMetricsBase; // Lorg/bstats/bukkit/Metrics;
    // -> metricsBase:Lorg/bstats/bukkit/MetricsBase;
    private static final Method mSendData;  // Lorg/bstats/MetricsBase; -> sendData()V

    private static ScenamaticaMetrics instance;

    static
    {
        try
        {
            fMetricsBase = Metrics.class.getDeclaredField("metricsBase");
            fMetricsBase.setAccessible(true);

            mSendData = MetricsBase.class.getDeclaredMethod("startSubmitting");
            mSendData.setAccessible(true);
        }
        catch (NoSuchFieldException | NoSuchMethodException e)
        {
            throw new IllegalStateException(e);
        }
    }

    private final MetricsTestReporter reporter;
    private int totalTests;

    private ScenamaticaMetrics(Scenamatica scenamatica)
    {
        super(scenamatica, SCENAMATICA_PLUGIN_ID);
        this.reporter = new MetricsTestReporter();

        this.totalTests = 0;

        this.addCustomChart(new SingleLineChart("tests_ran", () -> this.totalTests));
        this.addCustomChart(new SimplePie("ci", ScenamaticaMetrics::getCIState));
    }

    private static String getCIState()
    {
        String ci = System.getenv("CI");
        return ci == null ? "no" : "yes";
    }

    public static ScenamaticaMetrics init(Scenamatica scenamatica)
    {
        if (instance != null)
            throw new IllegalStateException("ScenamaticaMetrics has already been initialized.");

        return instance = new ScenamaticaMetrics(scenamatica);
    }

    public static void connect(ReportersBridge bridge)
    {
        if (instance == null)
            throw new IllegalStateException("ScenamaticaMetrics has not been initialized.");

        bridge.addReporter(instance.reporter);
    }

    @SneakyThrows({IllegalAccessException.class, InvocationTargetException.class})
    public static void sendMetrics()
    {
        if (instance == null)
            throw new IllegalStateException("ScenamaticaMetrics has not been initialized.");
        else if (instance.totalTests == 0)
            return;  // 送信の必要がない。

        MetricsBase metricsBase = (MetricsBase) fMetricsBase.get(instance);
        mSendData.invoke(metricsBase);
    }

    private final class MetricsTestReporter extends AbstractTestReporter
    {
        @Override
        public void onTestSessionEnd(@NotNull ScenarioSession session)
        {
            ScenamaticaMetrics.this.totalTests += (int) session.getScenarios().stream()
                    .filter(scenario -> scenario.getResult() != null)
                    .count();
        }
    }
}
