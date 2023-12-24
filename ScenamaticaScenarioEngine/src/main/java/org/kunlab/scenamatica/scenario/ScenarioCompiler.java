package org.kunlab.scenamatica.scenario;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import org.kunlab.scenamatica.commons.utils.LogUtils;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionRunManager;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioActionListener;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledTriggerAction;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;
import org.kunlab.scenamatica.scenario.runtime.CompiledTriggerActionImpl;
import org.kunlab.scenamatica.scenario.runtime.InternalCompiler;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ScenarioCompiler
{
    private final ScenamaticaRegistry registry;
    private final ScenarioEngine engine;

    private final ScenarioFileStructure scenario;
    private final ActionRunManager actionManager;
    private final ScenarioActionListener listener;
    private final String logPrefix;
    private final int compileNeeded;

    private int compiled;

    public ScenarioCompiler(ScenarioEngine engine, ScenamaticaRegistry registry, ActionRunManager actionManager)
    {
        this.engine = engine;
        this.registry = registry;
        this.scenario = engine.getScenario();
        this.actionManager = actionManager;
        this.listener = engine.getListener();
        this.logPrefix = LogUtils.gerScenarioPrefix(null, this.scenario);

        this.compileNeeded = InternalCompiler.calcCompileNeeded(this.scenario);
        this.compiled = 0;
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

    public CompiledScenarioAction compileRunIf(ActionStructure runIf)
    {
        this.logCompiling(++this.compiled, this.compileNeeded, "RUN_IF");

        return InternalCompiler.compileConditionAction(
                this.registry,
                this.engine,
                this.actionManager.getCompiler(),
                this.listener,
                runIf
        );
    }

    public List<CompiledScenarioAction> compileMain(List<? extends ScenarioStructure> scenarios)
    {
        this.logCompilingMain(++this.compiled, this.compileNeeded);
        return InternalCompiler.compileActions(
                this.registry,
                this.engine,
                this.actionManager.getCompiler(),
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
                beforeActions = this.compileMain(trigger.getBeforeThat());
            }
            else
                beforeActions = new ArrayList<>();

            if (!trigger.getAfterThat().isEmpty())
            {
                this.logCompiling(++compiled, this.compileNeeded, "AFTER", triggerType);
                afterActions = this.compileMain(trigger.getAfterThat());
            }
            else
                afterActions = new ArrayList<>();

            CompiledScenarioAction runIf = null;
            if (trigger.getRunIf() != null)
                runIf = InternalCompiler.compileConditionAction(
                        this.registry,
                        this.engine,
                        this.actionManager.getCompiler(),
                        this.listener,
                        trigger.getRunIf()
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

    private void logCompiling(int compiled, int total, String type, TriggerType triggerType)
    {
        MsgArgs newArgs = MsgArgs.of("compiled", compiled)
                .add("total", total)
                .add("type", type)
                .add("triggerType", triggerType.name())
                .add("scenarioName", this.scenario.getName());
        this.logWithPrefix(Level.INFO, LangProvider.get("scenario.compile.child", newArgs));
    }

    private void logCompiling(int compiled, int total, String type)
    {
        MsgArgs newArgs = MsgArgs.of("compiled", compiled)
                .add("total", total)
                .add("type", type)
                .add("triggerType", "MAIN")
                .add("scenarioName", this.scenario.getName());
        this.logWithPrefix(Level.INFO, LangProvider.get("scenario.compile.child", newArgs));
    }

    private void logCompilingMain(int compiled, int total)
    {
        MsgArgs newArgs = MsgArgs.of("compiled", compiled)
                .add("total", total)
                .add("type", "main")
                .add("scenarioName", this.scenario.getName());
        this.logWithPrefix(Level.INFO, LangProvider.get("scenario.compile.main", newArgs));
    }

    private void logWithPrefix(Level level, String message)
    {
        this.registry.getLogger().log(
                level,
                this.logPrefix + message
        );
    }

}
