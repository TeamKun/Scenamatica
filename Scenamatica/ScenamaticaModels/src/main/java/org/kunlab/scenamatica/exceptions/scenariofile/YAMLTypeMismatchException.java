package org.kunlab.scenamatica.exceptions.scenariofile;

import org.kunlab.scenamatica.enums.YAMLNodeType;

public class YAMLTypeMismatchException extends YamlParsingException
{
    private final YAMLNodeType expectedType;
    private final YAMLNodeType actualType;

    public YAMLTypeMismatchException(String fileName, String keyName, int line, String[] aroundLines, YAMLNodeType expectedType, YAMLNodeType actualType)
    {
        super("Value type mismatch", fileName, keyName, line, aroundLines);

        this.expectedType = expectedType;
        this.actualType = actualType;
    }

    @Override
    public String getMessage()
    {
        return super.getMessage() + " Expected: " + expectedType + ", Actual: " + actualType;
    }
}
