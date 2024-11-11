package org.kunlab.scenamatica.exceptions.scenariofile;

public class YamlMissingKeyValueException extends YamlParsingException
{

    public YamlMissingKeyValueException(String message, String fileName, String targetKey, int line, String[] aroundLines, Throwable exception)
    {
        super(message, fileName, targetKey, line, aroundLines, exception);
    }

    public YamlMissingKeyValueException(String message, String fileName, String targetKey, int line, String[] aroundLines)
    {
        super(message, fileName, targetKey, line, aroundLines);
    }

    @Override
    public String getMessage()
    {
        return super.getMessage() + "\n" +
                "----------------------------------------\n" +
                "[!] The value of key '" + this.getTargetKey() + "' is missing.\n" +
                "[!] You must set a value for this key.\n";
    }
}
