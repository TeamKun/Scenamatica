package org.kunlab.scenamatica.scenario;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.LogUtils;
import org.kunlab.scenamatica.enums.RunAs;
import org.kunlab.scenamatica.enums.RunOn;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.interfaces.action.ActionCompiler;
import org.kunlab.scenamatica.interfaces.action.ActionManager;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioActionListener;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.structures.scenario.ActionStructure;
import org.kunlab.scenamatica.interfaces.structures.scenario.ScenarioStructure;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;
import org.kunlab.scenamatica.scenario.runtime.CompiledScenarioActionImpl;
import org.kunlab.scenamatica.scenario.runtime.CompiledTriggerActionImpl;
import org.kunlab.scenamatica.scenario.runtime.ScenarioCompilationErrorException;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScenarioCompiler
{
    private final ScenarioEngine engine;
    private final Logger logger;
    private final boolean isVerbose;

    private final ScenarioFileStructure scenario;
    private final ActionCompiler actionCompiler;
    private final ScenarioActionListener listener;
    private final String logPrefix;
    private final int compileNeeded;

    private int compiled;

    public ScenarioCompiler(ScenarioEngine engine, Logger logger, boolean isVerbose, ActionManager actionManager)
    {
        this.engine = engine;
        this.logger = logger;
        this.isVerbose = isVerbose;
        this.scenario = engine.getScenario();
        this.actionCompiler = actionManager.getCompiler();
        this.listener = engine.getListener();
        this.logPrefix = LogUtils.gerScenarioPrefix(null, this.scenario);

        this.compileNeeded = calcCompileNeeded(this.scenario);
        this.compiled = 0;
    }

    public static int calcCompileNeeded(ScenarioFileStructure scenario)
    {
        int compileNeeded = 1; // 本シナリオの分。

        for (TriggerStructure trigger : scenario.getTriggers())
        {
            if (!trigger.getBeforeThat().isEmpty())
                compileNeeded++;
            if (!trigger.getAfterThat().isEmpty())
                compileNeeded++;
        }

        if (scenario.getRunIf() != null)
            compileNeeded++;

        return compileNeeded;
    }

    public void notifyCompileStart()
    {
        this.logWithPrefix(
                Level.INFO,
                LangProvider.get(
                        "scenario.compile.start",
                        MsgArgs.of("scenarioName", this.scenario.getName())
                )
        );
    }

    public void notifyCompileEnd()
    {
        this.logWithPrefix(
                Level.INFO,
                LangProvider.get(
                        "scenario.compile.end",
                        MsgArgs.of("scenarioName", this.scenario.getName())
                                .add("compiled", this.compiled)
                )
        );
    }

    public CompiledScenarioAction compileRunIf(ActionStructure runIf)
    {
        this.logCompiling(++this.compiled, this.compileNeeded, "RUN_IF");

        return this.compileConditionAction(RunOn.RUNIF, RunAs.RUNIF, this.listener, runIf);
    }

    public List<CompiledScenarioAction> compileMain(RunOn runOn, RunAs runAs, List<? extends ScenarioStructure> scenarios)
    {
        this.logCompilingMain(++this.compiled, this.compileNeeded);
        return this.compileActions(
                runOn,
                runAs,
                this.listener,
                scenarios
        );
    }

    public List<CompiledTriggerAction> compileTriggerActions(List<? extends TriggerStructure> triggers)
    {
        int compiled = 1; // 本シナリオの分。

        List<CompiledTriggerAction> triggerActions = new ArrayList<>();
        for (TriggerStructure trigger : triggers)
        {
            List<CompiledScenarioAction> beforeActions;
            List<CompiledScenarioAction> afterActions;

            TriggerType triggerType = trigger.getType();
            if (!trigger.getBeforeThat().isEmpty())
            {
                this.logCompiling(++compiled, this.compileNeeded, "BEFORE", triggerType);
                beforeActions = this.compileMain(RunOn.TRIGGER, RunAs.BEFORE, trigger.getBeforeThat());
            }
            else
                beforeActions = new ArrayList<>();

            if (!trigger.getAfterThat().isEmpty())
            {
                this.logCompiling(++compiled, this.compileNeeded, "AFTER", triggerType);
                afterActions = this.compileMain(RunOn.TRIGGER, RunAs.AFTER, trigger.getAfterThat());
            }
            else
                afterActions = new ArrayList<>();

            CompiledScenarioAction runIf = null;
            if (trigger.getRunIf() != null)
                runIf = this.compileConditionAction(RunOn.TRIGGER,
                        RunAs.RUNIF,
                        this.listener, trigger.getRunIf()
                );

            triggerActions.add(new CompiledTriggerActionImpl(
                    trigger,
                    beforeActions,
                    afterActions,
                    runIf
            ));
        }

        return triggerActions;
    }

    public CompiledScenarioAction compileConditionAction(
            @NotNull RunOn runOn, @NotNull RunAs runAs, @NotNull ScenarioActionListener listener,
            @NotNull ActionStructure structure)
    {  // RunIF 用に偽装する。
        CompiledAction action;
        try
        {

            action = this.actionCompiler.compile(
                    this.engine,
                    ScenarioType.CONDITION_REQUIRE,
                    runOn,
                    runAs,
                    structure,
                    listener::onActionError,
                    (result, type) -> listener.onActionExecutionFinished(result)
            );
        }

        catch (Throwable e)
        {
            throw new ScenarioCompilationErrorException(e, this.engine.getScenario().getName(), structure.getType());
        }

        return new CompiledScenarioActionImpl(
                new ScenarioStructure()
                {
                    @Override
                    public @NotNull ScenarioType getType()
                    {
                        return ScenarioType.CONDITION_REQUIRE;
                    }

                    @Override
                    public @NotNull ActionStructure getAction()
                    {
                        return structure;
                    }

                    @Override
                    public ActionStructure getRunIf()
                    {
                        return null;
                    }

                    @Override
                    public long getTimeout()
                    {
                        return -1;
                    }

                    @Override
                    public String getName()
                    {
                        return null;
                    }
                },
                ScenarioType.CONDITION_REQUIRE,
                action,
                null
        );
    }

    public List<CompiledScenarioAction> compileActions(@NotNull RunOn runOn, @NotNull RunAs runAs,
                                                       @NotNull ScenarioActionListener listener,
                                                       @NotNull List<? extends ScenarioStructure> scenarios)
    {
        List<CompiledScenarioAction> compiled = new ArrayList<>();
        for (ScenarioStructure scenario : scenarios)
            compiled.add(this.compileAction(runOn, runAs, listener, scenario));

        return compiled;
    }

    public CompiledScenarioAction compileAction(
            @NotNull RunOn runOn, @NotNull RunAs runAs, @NotNull ScenarioActionListener listener,
            @NotNull ScenarioStructure scenario)
    {
        try
        {
            CompiledAction action = this.actionCompiler.compile(
                    this.engine,
                    scenario.getType(),
                    runOn,
                    runAs,
                    scenario.getAction(),
                    listener::onActionError,
                    (result, type) -> listener.onActionExecutionFinished(result)
            );

            action.getContext().setScenarioName(scenario.getName());
            scenario.getType().validatePerformableActionType(action.getExecutor().getClass());

            return new CompiledScenarioActionImpl(
                    scenario,
                    scenario.getType(),
                    action,
                    scenario.getRunIf() == null ? null:
                            this.compileConditionAction(runOn, RunAs.RUNIF, listener, scenario.getRunIf())
            );
        }
        catch (Throwable e)
        {
            throw new ScenarioCompilationErrorException(e, this.engine.getScenario().getName(), scenario.getAction().getType());
        }
    }

    private void logCompiling(int compiled, int total, String type, TriggerType triggerType)
    {
        if (!this.isVerbose)
            return;

        MsgArgs newArgs = MsgArgs.of("compiled", compiled)
                .add("total", total)
                .add("type", type)
                .add("triggerType", triggerType.name())
                .add("scenarioName", this.scenario.getName());
        this.logWithPrefix(Level.INFO, LangProvider.get("scenario.compile.child", newArgs));
    }

    private void logCompiling(int compiled, int total, String type)
    {
        if (!this.isVerbose)
            return;

        MsgArgs newArgs = MsgArgs.of("compiled", compiled)
                .add("total", total)
                .add("type", type)
                .add("triggerType", "MAIN")
                .add("scenarioName", this.scenario.getName());
        this.logWithPrefix(Level.INFO, LangProvider.get("scenario.compile.child", newArgs));
    }

    private void logCompilingMain(int compiled, int total)
    {
        if (!this.isVerbose)
            return;

        MsgArgs newArgs = MsgArgs.of("compiled", compiled)
                .add("total", total)
                .add("type", "main")
                .add("scenarioName", this.scenario.getName());
        this.logWithPrefix(Level.INFO, LangProvider.get("scenario.compile.main", newArgs));
    }

    private void logWithPrefix(Level level, String message)
    {
        this.logger.log(
                level,
                this.logPrefix + message
        );
    }

}
