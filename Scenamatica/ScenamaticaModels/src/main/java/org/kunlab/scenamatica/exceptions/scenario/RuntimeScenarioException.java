package org.kunlab.scenamatica.exceptions.scenario;

import lombok.Getter;
import org.kunlab.scenamatica.enums.ScenarioResultCause;

/**
 * シナリオの実行に関する例外を表すクラスです。
 */
@Getter
public class RuntimeScenarioException extends RuntimeException
{
    private final ScenarioResultCause causeToFail;
    private final String[] detailedMessages;

    public RuntimeScenarioException(ScenarioResultCause causeToFail, String message, String... detailedMessages)
    {
        super(message);
        this.causeToFail = causeToFail;
        this.detailedMessages = detailedMessages;
    }

    public RuntimeScenarioException(Throwable cause)
    {
        super(cause);
        this.causeToFail = ScenarioResultCause.INTERNAL_ERROR;
        this.detailedMessages = new String[0];
    }

    public RuntimeScenarioException(String message, ScenarioResultCause causeToFail, Throwable cause, String... detailedMessages)
    {
        super(message, cause);
        this.causeToFail = causeToFail;
        this.detailedMessages = detailedMessages;
    }
}
