package net.kunmc.lab.scenamatica.scenario;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.scenamatica.enums.ScenarioResultCause;
import net.kunmc.lab.scenamatica.enums.ScenarioState;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioResult;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    Action<?> failedAction;

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
