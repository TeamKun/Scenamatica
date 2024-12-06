package org.kunlab.scenamatica.exceptions.scenario;

import org.kunlab.scenamatica.enums.ScenarioResultCause;

public class IllegalScenarioStateException extends RuntimeScenarioException
{
    public IllegalScenarioStateException(String message)
    {
        super(ScenarioResultCause.ILLEGAL_CONDITION, message);
    }
}
