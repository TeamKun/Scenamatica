package org.kunlab.scenamatica.scenario;

import lombok.experimental.StandardException;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.exceptions.scenario.RuntimeScenarioException;

@StandardException
public class ScenarioWaitTimedOutException extends RuntimeScenarioException
{
    public ScenarioWaitTimedOutException()
    {
        super(ScenarioResultCause.SCENARIO_TIMED_OUT, "Scenario wait timed out");
    }
}
