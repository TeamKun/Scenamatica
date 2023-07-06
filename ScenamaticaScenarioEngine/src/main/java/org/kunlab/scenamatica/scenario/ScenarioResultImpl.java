package org.kunlab.scenamatica.scenario;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.enums.ScenarioState;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileBean;

import java.util.UUID;

@Value
@AllArgsConstructor
public class ScenarioResultImpl implements ScenarioResult
{
    @NotNull
    ScenarioFileBean scenario;
    @NotNull
    UUID testID;
    @NotNull
    ScenarioState state;
    @NotNull
    ScenarioResultCause scenarioResultCause;
    long startedAt;
    long finishedAt;

    @Nullable
    Action<? extends ActionArgument> failedAction;

    public ScenarioResultImpl(@NotNull ScenarioFileBean scenario,
                              @NotNull UUID testID,
                              @NotNull ScenarioState state,
                              @NotNull ScenarioResultCause resultType,
                              long startedAt)
    {
        this.scenario = scenario;
        this.testID = testID;
        this.state = state;
        this.scenarioResultCause = resultType;
        this.startedAt = startedAt;
        this.finishedAt = System.currentTimeMillis();
        this.failedAction = null;
    }
}
