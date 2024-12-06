package org.kunlab.scenamatica.scenario;

import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.exceptions.scenario.RuntimeScenarioException;

public class ScenarioWaitTimedOutException extends RuntimeScenarioException
{
    public ScenarioWaitTimedOutException()
    {
        super(ScenarioResultCause.SCENARIO_TIMED_OUT, "Scenario wait timed out");
    }

    public ScenarioWaitTimedOutException(String message)
    {
        super(ScenarioResultCause.SCENARIO_TIMED_OUT, message);
    }
}
