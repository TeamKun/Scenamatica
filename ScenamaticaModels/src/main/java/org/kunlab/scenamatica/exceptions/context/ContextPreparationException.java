package org.kunlab.scenamatica.exceptions.context;

/**
 * コンテキストの準備中に発生した例外です。
 */
public class ContextPreparationException extends Exception
{
    private final String message;

    public ContextPreparationException(String message)
    {
        super("An error occurred while preparing context: " + message);
        this.message = message;
    }

    public ContextPreparationException(String message, Throwable cause)
    {
        super("An error occurred while preparing context: " + message, cause);
        this.message = message;
    }

    public ContextPreparationException(Throwable cause)
    {
        super("An error occurred while preparing context", cause);
        this.message = cause.getMessage();
    }
}
