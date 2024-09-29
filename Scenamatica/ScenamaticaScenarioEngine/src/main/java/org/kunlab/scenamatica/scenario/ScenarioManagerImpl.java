package org.kunlab.scenamatica.scenario;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.SneakyThrows;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioAlreadyRunningException;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioException;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioNotFoundException;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioNotRunningException;
import org.kunlab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionManager;
import org.kunlab.scenamatica.interfaces.scenario.MilestoneManager;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioManager;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenario.SessionCreator;
import org.kunlab.scenamatica.interfaces.scenario.SessionStorage;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;
import org.kunlab.scenamatica.scenario.engine.ScenarioEngineImpl;
import org.kunlab.scenamatica.scenario.milestone.MilestoneManagerImpl;
import org.kunlab.scenamatica.scenario.runtime.ScenarioCompilationErrorException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Getter
public class ScenarioManagerImpl implements ScenarioManager
{
    private final ScenamaticaRegistry registry;
    private final ActionManager actionManager;
    @Getter
    private final MilestoneManager milestoneManager;
    private final Multimap<Plugin, ScenarioEngine> engines;
    private final ScenarioQueue queue;
    @NotNull
    @Getter(AccessLevel.PACKAGE)
    private final TestReporter testReporter;
    private final TickListener tickListener;

    @Getter
    @Nullable
    private ScenarioEngine currentScenario;
    @Getter
    private boolean enabled;

    public ScenarioManagerImpl(@NotNull ScenamaticaRegistry registry)
    {
        this.registry = registry;
        this.actionManager = registry.getActionManager();
        this.testReporter = registry.getTestReporter();
        this.milestoneManager = new MilestoneManagerImpl();
        this.engines = ArrayListMultimap.create();
        this.tickListener = new TickListener(registry, this);
        this.queue = new ScenarioQueue(registry, this);
        this.currentScenario = null;
        this.enabled = true;
    }

    @Override
    public void init()
    {
        this.tickListener.init();
        this.queue.start();
    }

    @Override
    public boolean isRunning()
    {
        return this.currentScenario != null && this.currentScenario.isRunning();
    }

    @Override
    @NotNull
    public ScenarioResult startScenarioInterrupt(@NotNull Plugin plugin, @NotNull String scenarioName, boolean cancelRunning)
            throws ScenarioException
    {
        return this.startScenarioInterrupt(plugin, scenarioName, TriggerType.MANUAL_DISPATCH, cancelRunning);
    }

    @SneakyThrows(ScenarioNotRunningException.class)
    @Override
    @NotNull
    public ScenarioResult startScenarioInterrupt(@NotNull Plugin plugin, @NotNull String scenarioName, @NotNull TriggerType triggerType, boolean cancelRunning)
            throws ScenarioAlreadyRunningException, TriggerNotFoundException, ScenarioNotFoundException
    {
        if (!this.enabled)
            throw new IllegalStateException("Scenamatica is disabled.");
        else if (this.isRunning() && !cancelRunning)
            throw new ScenarioAlreadyRunningException("Another scenario is running.");

        AtomicReference<ScenarioResult> result = new AtomicReference<>();
        CyclicBarrier barrier = new CyclicBarrier(2);
        Consumer<ScenarioResult> callback = r -> {
            result.set(r);
            try
            {
                barrier.await();
            }
            catch (Exception e)
            {
                this.registry.getExceptionHandler().report(e);
            }
        };

        Pair<ScenarioEngine, TriggerStructure> runInfo = this.getRunInfoOrThrow(plugin, scenarioName, triggerType);
        ScenarioEngine engine = runInfo.getLeft();
        TriggerStructure trigger = runInfo.getRight();

        this.queue.addInterrupt(engine, trigger, callback);
        if (this.isRunning())  // cancelRunning == true
            this.cancel();
        try

        {
            barrier.await();
        }
        catch (Exception e)
        {
            this.registry.getExceptionHandler().report(e);
        }

        return result.get();
    }

    @Override
    public void queueScenario(@NotNull Plugin plugin, @NotNull String scenarioName, @NotNull TriggerType triggerType, int maxAttemptCount) throws ScenarioNotFoundException, TriggerNotFoundException
    {
        Pair<ScenarioEngine, TriggerStructure> runInfo = this.getRunInfoOrThrow(plugin, scenarioName, triggerType);
        ScenarioEngine engine = runInfo.getLeft();
        TriggerStructure trigger = runInfo.getRight();

        this.queue.add(engine, trigger, null, maxAttemptCount);
    }

    @Override
    public void queueScenario(SessionCreator sessionDefinition) throws ScenarioNotFoundException, TriggerNotFoundException
    {
        this.queue.addAll(sessionDefinition);
    }

    @Override
    public SessionCreator newSession()
    {
        return new SessionCreatorImpl(this);
    }

    @Override
    public void dequeueScenarios(@NotNull Plugin plugin)
    {
        this.queue.removeAll(plugin);
    }

    @Override
    public void dequeueScenario(@NotNull Plugin plugin, @NotNull String scenarioName)
    {
        this.queue.remove(plugin, scenarioName);
    }

    /* non-public */ Pair<ScenarioEngine, TriggerStructure> getRunInfoOrThrow(Plugin plugin, String scenarioName, TriggerType trigger)
            throws ScenarioNotFoundException, TriggerNotFoundException
    {
        ScenarioEngine engine = this.engines.get(plugin).stream()
                .filter(e -> e.getScenario().getName().equals(scenarioName))
                .findFirst()
                .orElseThrow(() -> new ScenarioNotFoundException(scenarioName));


        return Pair.of(engine, this.getTriggerOrThrow(engine, trigger));
    }

    /* non-pubic */ TriggerStructure getTriggerOrThrow(@NotNull ScenarioEngine engine, TriggerType type) throws TriggerNotFoundException
    {
        TriggerStructure trigger = this.getTriggerOrNull(engine, type);
        if (trigger == null)
            throw new TriggerNotFoundException(engine.getScenario().getName(), type);
        else
            return trigger;
    }

    @Nullable
        /* non-pubic */ TriggerStructure getTriggerOrNull(@NotNull ScenarioEngine engine, TriggerType type)
    {
        return engine.getTriggerActions().stream()
                .map(CompiledTriggerAction::getTrigger)
                .filter(t -> t.getType() == type)
                .findFirst()
                .orElse(null);
    }

    /* non-public */ ScenarioResult runScenario(ScenarioEngine engine, TriggerStructure trigger, SessionStorage variable, int attempted)
    {
        if (!engine.getPlugin().isEnabled())
            throw new IllegalStateException("Plugin is disabled.");
        else if (!this.enabled)
            throw new IllegalStateException("Scenamatica is disabled.");

        this.currentScenario = engine;
        ScenarioResult result;
        try
        {
            result = engine.start(trigger, variable, attempted);
        }
        catch (Throwable e)
        {
            this.registry.getExceptionHandler().report(e);
            result = new ScenarioResultImpl(
                    engine.getScenario(),
                    engine.getTestID(),
                    engine.getState(),
                    ScenarioResultCause.INTERNAL_ERROR,
                    Collections.emptyList(),
                    engine.getStartedAt(),
                    attempted,
                    null
            );
        }
        this.currentScenario = null;
        this.testReporter.onTestEnd(engine, result);

        return result;
    }

    @Override
    public void cancel() throws ScenarioNotRunningException
    {
        if (this.currentScenario == null)
            throw new ScenarioNotRunningException();
        else if (!this.currentScenario.isRunning())
            throw new ScenarioNotRunningException(this.currentScenario.getScenario().getName());

        this.currentScenario.cancel();
    }

    @Override
    public void unloadPluginScenarios(@NotNull Plugin plugin)
    {
        this.milestoneManager.revokeAllMilestones(plugin);
        Iterator<ScenarioEngine> iterator = this.engines.get(plugin).iterator();
        while (iterator.hasNext())
        {
            ScenarioEngine engine = iterator.next();
            if (engine.isRunning())
                engine.cancel();

            this.registry.getTriggerManager().unregisterTrigger(engine);

            iterator.remove();
        }

        this.registry.getScenarioFileManager().unloadPluginScenarios(plugin);
    }

    @Override
    public boolean loadPluginScenarios(@NotNull Plugin plugin)
    {
        // 内部でリロードを呼び出す（やることは同じなので）
        if (!this.registry.getScenarioFileManager().reloadPluginScenarios(plugin))
            return false;

        // エンジンのコンパイルも行う。
        Map<String, ScenarioFileStructure> scenarios = this.registry.getScenarioFileManager().getPluginScenarios(plugin);
        assert scenarios != null;

        List<ScenarioEngine> engines = this.createEnginesByFile(plugin, scenarios.values());

        this.registry.getTriggerManager().performTriggerFire(engines, TriggerType.ON_LOAD);

        return true;
    }

    private List<ScenarioEngine> createEnginesByFile(@NotNull Plugin owningPlugin, @NotNull Collection<ScenarioFileStructure> files)
    {
        Logger logger = this.registry.getLogger();

        List<ScenarioEngine> engines = new ArrayList<>(files.size());
        logger.info(LangProvider.get(
                "scenario.load.start",
                MsgArgs.of("pluginName", owningPlugin.getName())
        ));

        boolean isSuccessful = true;
        try
        {
            files.stream()
                    .map(scenario -> new ScenarioEngineImpl(
                                    this.registry,
                                    this,
                                    this.actionManager,
                                    this.testReporter,
                                    owningPlugin,
                                    scenario
                            )
                    )
                    .forEach(engines::add);
        }
        catch (ScenarioCompilationErrorException e)
        {
            isSuccessful = false;

            String message;
            if (e.getCause() == null)
                message = e.getMessage();
            else
                message = e.getCause().getMessage();

            logger.warning(LangProvider.get(
                    "scenario.compile.error",
                    MsgArgs.of("pluginName", owningPlugin.getName())
                            .add("scenarioName", e.getScenarioName())
                            .add("message", message)
            ));

            // 想定されている例外（e.g. アクションが見つからない 等）なので, レポートは不要
        }
        catch (RuntimeException e)
        {
            isSuccessful = false;

            logger.warning(LangProvider.get(
                    "scenario.compile.error",
                    MsgArgs.of("pluginName", owningPlugin.getName())
                            .add("message", e.getMessage())
            ));

            this.registry.getExceptionHandler().report(e);
        }

        String finishedMessageKey = isSuccessful ? "scenario.load.success": "scenario.load.cancel";
        logger.info(LangProvider.get(
                finishedMessageKey,
                MsgArgs.of("pluginName", owningPlugin.getName())
        ));

        if (isSuccessful)
            this.engines.putAll(owningPlugin, engines);
        else
            engines.clear();  // エラーが発生したときに, 一部のエンジンが追加されている可能性があるため
        return engines;
    }

    @Override
    public boolean reloadPluginScenarios(@NotNull Plugin plugin)
    {
        this.unloadPluginScenarios(plugin);
        return this.loadPluginScenarios(plugin);
    }

    @Override
    public @NotNull List<ScenarioEngine> getEnginesFor(@NotNull Plugin plugin)
    {
        return new ArrayList<>(this.engines.get(plugin));
    }

    /**
     * すべてのエンジンを返します。
     *
     * @return シナリオ
     */
    @Override
    public @NotNull List<ScenarioEngine> getEngines()
    {
        return new ArrayList<>(this.engines.values());
    }

    @Override
    public @Nullable ScenarioEngine getEngine(@NotNull Plugin plugin, @NotNull String scenarioName)
    {
        return this.engines.get(plugin).stream()
                .filter(e -> e.getScenario().getName().equals(scenarioName))
                .findFirst()
                .orElse(null);
    }

    @SneakyThrows(ScenarioNotRunningException.class)
    @Override
    public void shutdown()
    {
        if (this.isRunning())
            this.cancel();

        this.queue.shutdown();
        this.engines.clear();
    }

    @Override
    @SneakyThrows(ScenarioNotRunningException.class)
    public void setEnabled(boolean enabled)
    {
        if (this.enabled == enabled)
            return;
        else if (!enabled)
        {
            if (this.isRunning())
                this.cancel();

            this.queue.shutdown();
        }
        else
            this.queue.start();

        this.enabled = enabled;
    }

    @Override
    public void invalidate(@NotNull ScenarioEngine engine)
    {
        this.engines.remove(engine.getPlugin(), engine);
        if (engine.isRunning())
            engine.cancel();
    }

    @Override
    public void invalidate(@NotNull Plugin plugin)
    {
        this.engines.removeAll(plugin).forEach(ScenarioEngine::cancel);
    }
}
