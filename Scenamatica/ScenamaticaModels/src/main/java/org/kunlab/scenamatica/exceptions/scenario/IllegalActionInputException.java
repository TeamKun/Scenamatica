package org.kunlab.scenamatica.exceptions.scenario;

import lombok.Getter;
import org.kunlab.scenamatica.enums.ScenarioResultCause;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;

@Getter
public class IllegalActionInputException extends RuntimeScenarioException
{
    private final InputToken<?> token;

    public IllegalActionInputException(InputToken<?> token, String message)
    {
        super(ScenarioResultCause.ACTION_EXECUTION_FAILED, message);
        this.token = token;
    }

    public IllegalActionInputException(String message)
    {
        super(ScenarioResultCause.ACTION_EXECUTION_FAILED, message);
        this.token = null;
    }


}
