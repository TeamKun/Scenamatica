package net.kunmc.lab.scenamatica.scenario;

import lombok.Getter;
import lombok.Setter;
import net.kunmc.lab.scenamatica.enums.TestResultCause;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.action.WatchingEntry;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioActionListener;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.TestReporter;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
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
        this.setResult(TestResultCause.ACTION_EXECUTION_FAILED, action.getAction());
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
        if (this.waitingFor != null
                && entry.getAction().getClass() == this.waitingFor.getAction().getClass()
                && entry.getArgument().isSame(this.waitingFor.getArgument()))
        {
            this.reporter.onWatchingActionExecuted(this.engine, entry.getAction());
            this.setPassed();
        }
        else  // 他のアクションが実行された。
        {
            this.reporter.onActionJumped(this.engine, entry.getAction(), this.waitingFor);
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
}
