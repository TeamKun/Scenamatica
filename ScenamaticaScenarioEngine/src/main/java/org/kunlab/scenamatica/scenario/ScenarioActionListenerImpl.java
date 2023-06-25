package org.kunlab.scenamatica.scenario;

import lombok.Getter;
import lombok.Setter;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatchingEntry;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioActionListener;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ScenarioActionListenerImpl implements ScenarioActionListener
{
    private final ScenarioEngine engine;
    private final TestReporter reporter;

    @Getter
    @Setter
    @Nullable
    private CompiledScenarioAction<?> waitingFor;

    public ScenarioActionListenerImpl(ScenarioEngine engine, ScenamaticaRegistry registry)
    {
        this.engine = engine;
        this.reporter = registry.getTestReporter();
    }

    @Override
    public <A extends ActionArgument> void onActionError(@NotNull CompiledAction<A> action, @NotNull Throwable error)
    {
        this.reporter.onActionExecuteFailed(this.engine, action, error);
        this.setResult(ScenarioResultCause.ACTION_EXECUTION_FAILED, action.getExecutor());
    }

    @Override
    public <A extends ActionArgument> void onActionExecuted(@NotNull CompiledAction<A> action)
    {
        this.reporter.onActionSuccess(this.engine, action);
        this.setPassed();
    }

    @Override
    public <A extends ActionArgument> void onActionFired(@NotNull WatchingEntry<A> entry, @NotNull Event event)
    {
        CompiledAction<A> action = entry.getAction();
        Action<A> executor = action.getExecutor();

        if (this.waitingFor == null)
            return;

        if (executor.getClass() == this.waitingFor.getAction().getExecutor().getClass()
                && action.getArgument().isSame(this.waitingFor.getAction().getArgument()))
        {
            this.reporter.onWatchingActionExecuted(this.engine, entry.getAction());
            this.setPassed();
        }
        else  // 他のアクションが実行された。
        {
            this.reporter.onActionJumped(this.engine, action, this.waitingFor.getAction());
            this.setResult(
                    ScenarioResultCause.ACTION_EXPECTATION_JUMPED,
                    executor
            );
        }
    }

    private void setResult(ScenarioResultCause cause, @Nullable Action<?> failedAction)
    {
        this.engine.getDeliverer().setResult(new ScenarioResultImpl(
                this.engine.getScenario(),
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
        this.setResult(ScenarioResultCause.PASSED, null);
    }
}
