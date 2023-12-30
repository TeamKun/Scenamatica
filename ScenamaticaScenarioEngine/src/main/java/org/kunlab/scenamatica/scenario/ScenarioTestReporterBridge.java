package org.kunlab.scenamatica.scenario;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioActionListener;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.scenario.engine.ScenarioEngineImpl;

public class ScenarioTestReporterBridge implements ScenarioActionListener
{
    private final ScenarioEngineImpl engine;
    private final TestReporter reporter;

    @Getter
    @Setter
    @Nullable
    private CompiledScenarioAction waitingFor;

    public ScenarioTestReporterBridge(ScenarioEngineImpl engine, ScenamaticaRegistry registry)
    {
        this.engine = engine;
        this.reporter = registry.getTestReporter();
    }

    @Override
    public void onActionFinished(@NotNull ActionResult result, @NotNull ScenarioType type)
    {
        if (type == ScenarioType.ACTION_EXPECT)
            this.onWatcherFinished(result);
        else /* if (type == ScenarioType.ACTION_EXECUTE) */
            this.reporter.onActionSuccess(this.engine, result);

        this.waitingFor = null;
        this.engine.getDeliverer().setResult(result);
    }

    private void onWatcherFinished(ActionResult result)
    {
        if (this.waitingFor == null)
            return;

        CompiledAction action = this.waitingFor.getAction();

        if (result.getScenarioName().equals(action.getExecutor().getName())
                && result.getRunID().equals(action.getContext().getContextID()))
            this.reporter.onWatchingActionExecuted(this.engine, result);
        else  // 他のアクションが実行された。
            this.reporter.onActionJumped(this.engine, result, action);
    }

    @Override
    public void onActionError(CompiledAction action, Throwable throwable)
    {
        this.engine.getRegistry().getExceptionHandler().report(throwable);
        this.engine.getDeliverer().setExceptionCaught(throwable);
    }
}
