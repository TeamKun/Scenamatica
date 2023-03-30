package net.kunmc.lab.scenamatica.scenario;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import net.kunmc.lab.scenamatica.enums.TestResultCause;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.action.WatchingEntry;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioActionListener;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public class ScenarioActionListenerImpl implements ScenarioActionListener
{
    private final ScenarioEngine engine;
    private final ScenamaticaRegistry registry;

    @Getter
    @Setter
    @Nullable
    private CompiledScenarioAction<?> waitingFor;

    public ScenarioActionListenerImpl(ScenarioEngine engine, ScenamaticaRegistry registry)
    {
        this.engine = engine;
        this.registry = registry;
    }

    @Override
    public <A extends ActionArgument> void onActionError(@NotNull CompiledAction<A> action, @NotNull Throwable error)
    {
        this.log(Level.WARNING, "scenario.result.action.error", action.getAction());
        this.setResult(TestResultCause.ACTION_EXECUTION_FAILED, action.getAction());
    }

    @Override
    public <A extends ActionArgument> void onActionExecuted(@NotNull CompiledAction<A> action)
    {
        this.log(Level.INFO, "scenario.result.action.passed", action.getAction());
        this.setPassed();
    }

    @Override
    public <A extends ActionArgument> void onActionFired(@NotNull WatchingEntry<A> entry, @NotNull Event event)
    {
        if (this.waitingFor != null
                && entry.getAction().getClass() == this.waitingFor.getAction().getClass()
                && entry.getArgument().isSame(this.waitingFor.getArgument()))
        {
            this.log(Level.INFO, "scenario.result.watch.passed", entry.getAction());
            this.setPassed();
        }
        else  // 他のアクションが実行された。
        {
            this.log(Level.INFO, "scenario.result.action.jumped", entry.getAction());
            this.setResult(
                    TestResultCause.ACTION_EXPECTATION_JUMPED,
                    entry.getAction()
            );
        }
    }

    private void setResult(TestResultCause cause, @Nullable Action<?> failedAction)
    {
        this.engine.getDeliverer().setResult(new TestResultImpl(
                this.engine.getTestID(),
                this.engine.getState(),
                cause,
                this.engine.getStartedAt(),
                System.currentTimeMillis(),
                failedAction
        ));
    }

    private void setPassed()
    {
        this.setResult(TestResultCause.PASSED, null);
    }

    private void log(Level level, String key, Action<?> action)
    {
        this.registry.getLogger().log(
                level,
                LangProvider.get(
                        key,
                        MsgArgs.of("scenarioName", this.engine.getScenario().getName())
                                .add("actionName", action.getClass().getSimpleName())
                )
        );
    }
}
