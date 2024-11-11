package org.kunlab.scenamatica.exceptions.scenariofile;

public class YamlValueParsingException extends YamlParsingException
{
    public YamlValueParsingException(String message, String fileName, String targetKey, int line, String[] aroundLines)
    {
        super(message, fileName, targetKey, line, aroundLines);
    }

    public YamlValueParsingException(String message, String fileName, String targetKey, int line, String[] aroundLines, Throwable exception)
    {
        super(message, fileName, targetKey, line, aroundLines, exception);
    }

    @Override
    public String getMessage()
    {
        return this.getCause() == null ? super.getMessage(): this.getCause().getMessage();
    }
}
