package org.kunlab.scenamatica.exceptions.scenario;

import lombok.Getter;

/**
 * シナリオの実行に関する例外を表すクラスです。
 */
@Getter
public class RuntimeScenarioException extends RuntimeException
{
    private final String[] detailedMessages;

    public RuntimeScenarioException(String message, String... detailedMessages)
    {
        super(message);
        this.detailedMessages = detailedMessages;
    }

    public RuntimeScenarioException(Throwable cause)
    {
        super(cause);
        this.detailedMessages = new String[0];
    }

    public RuntimeScenarioException(String message, Throwable cause, String... detailedMessages)
    {
        super(message, cause);
        this.detailedMessages = detailedMessages;
    }
}
