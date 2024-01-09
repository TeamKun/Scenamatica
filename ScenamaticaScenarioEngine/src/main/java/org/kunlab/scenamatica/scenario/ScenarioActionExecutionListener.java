package org.kunlab.scenamatica.scenario;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioActionListener;
import org.kunlab.scenamatica.interfaces.scenario.TestReporter;
import org.kunlab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import org.kunlab.scenamatica.scenario.engine.ScenarioEngineImpl;

public class ScenarioActionExecutionListener implements ScenarioActionListener
{
    private final ScenarioEngineImpl engine;
    private final TestReporter reporter;

    @Getter
    @Setter
    @Nullable
    private CompiledScenarioAction waitingFor;

    public ScenarioActionExecutionListener(ScenarioEngineImpl engine, ScenamaticaRegistry registry)
    {
        this.engine = engine;
        this.reporter = registry.getTestReporter();
    }

    @Override
    public void onActionExecutionFinished(@NotNull ActionResult result)
    {
        this.reporter.onActionSuccess(this.engine, result);

        this.deliver(result);
    }

    @Override
    public void onObservingActionExecuted(@NotNull ActionResult result, boolean isJumped)
    {
        if (isJumped && this.waitingFor != null)  // 後に予期していたアクションが実行された
            this.reporter.onActionJumped(this.engine, result, this.waitingFor.getAction());
        else
            this.reporter.onWatchingActionExecuted(this.engine, result);


        this.deliver(result);
    }

    private void deliver(ActionResult result)
    {
        this.waitingFor = null;
        this.engine.getDeliverer().setResult(result);
    }

    @Override
    public void onActionError(CompiledAction action, Throwable throwable)
    {
        this.engine.getDeliverer().setExceptionCaught(throwable);
    }
}
