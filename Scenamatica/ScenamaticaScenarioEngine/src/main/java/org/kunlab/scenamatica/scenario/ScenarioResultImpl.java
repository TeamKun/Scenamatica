package org.kunlab.scenamatica.scenario;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.enums.ScenarioState;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioResult;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;

import java.util.List;
import java.util.UUID;

@Value
public class ScenarioResultImpl implements ScenarioResult
{
    @NotNull
    ScenarioFileStructure scenario;
    @NotNull
    UUID testID;
    @NotNull
    ScenarioState state;
    @NotNull
    ScenarioResultCause cause;
    long startedAt;
    long finishedAt;
    int attemptOf;

    @Nullable
    Action failedAction;

    public ScenarioResultImpl(@NotNull ScenarioFileStructure scenario,
                              @NotNull UUID testID,
                              @NotNull ScenarioState state,
                              @NotNull ScenarioResultCause resultType,
                              @NotNull List<ActionResult> actionResults,
                              long startedAt,
                              int attemptOf)
    {
        this.scenario = scenario;
        this.testID = testID;
        this.state = state;
        this.cause = resultType;
        this.startedAt = startedAt;
        this.finishedAt = System.currentTimeMillis();
        this.attemptOf = attemptOf;
        this.failedAction = null;
    }

    public ScenarioResultImpl(@NotNull ScenarioFileStructure scenario,
                              @NotNull UUID testID,
                              @NotNull ScenarioState state,
                              @NotNull ScenarioResultCause resultType,
                              @NotNull List<ActionResult> actionResults,
                              long startedAt,
                              int attemptOf,
                              @Nullable Action failedAction)
    {
        this.scenario = scenario;
        this.testID = testID;
        this.state = state;
        this.cause = resultType;
        this.startedAt = startedAt;
        this.finishedAt = System.currentTimeMillis();
        this.attemptOf = attemptOf;
        this.failedAction = failedAction;
    }
}
