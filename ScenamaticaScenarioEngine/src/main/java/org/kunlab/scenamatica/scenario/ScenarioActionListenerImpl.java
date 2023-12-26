package org.kunlab.scenamatica.scenario;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.WatchingEntry;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioActionListener;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.scenario.engine.ScenarioEngineImpl;

public class ScenarioActionListenerImpl implements ScenarioActionListener
{
    private final ScenarioEngineImpl engine;
    private final TestReporter reporter;

    @Getter
    @Setter
    @Nullable
    private CompiledScenarioAction waitingFor;

    public ScenarioActionListenerImpl(ScenarioEngineImpl engine, ScenamaticaRegistry registry)
    {
        this.engine = engine;
        this.reporter = registry.getTestReporter();
    }

    @Override
    public void onActionError(@NotNull CompiledAction action, @NotNull Throwable error)
    {
        this.reporter.onActionExecuteFailed(this.engine, action, error);
        this.setResult(ScenarioResultCause.ACTION_EXECUTION_FAILED, action.getExecutor());
    }

    @Override
    public void onActionExecuted(@NotNull CompiledAction action)
    {
        this.reporter.onActionSuccess(this.engine, action);
        this.setPassed();
    }

    @Override
    public void onActionFired(@NotNull WatchingEntry entry, @NotNull Event event)
    {
        CompiledAction action = entry.getAction();
        Action executor = action.getExecutor();

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

    private void setResult(ScenarioResultCause cause, @Nullable Action failedAction)
    {
        this.engine.getDeliverer().setResult(new ScenarioResultImpl(
                this.engine.getExecutor().getScenario(),
                this.engine.getExecutor().getTestID(),
                this.engine.getExecutor().getState(),
                cause,
                this.engine.getStartedAt(),
                System.currentTimeMillis(),
                this.engine.getExecutor().getAttemptedCount(),
                failedAction
        ));
    }

    private void setPassed()
    {
        this.setResult(ScenarioResultCause.PASSED, null);
    }
}
