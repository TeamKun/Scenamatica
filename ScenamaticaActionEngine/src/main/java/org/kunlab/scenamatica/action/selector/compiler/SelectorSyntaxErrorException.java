package org.kunlab.scenamatica.action.selector.compiler;

import lombok.experimental.StandardException;

@StandardException
public class SelectorSyntaxErrorException extends IllegalArgumentException
{
    public static SelectorSyntaxErrorException unexpectedToken(String given, String token, int index)
    {
        return new SelectorSyntaxErrorException(String.format("Unexpected token: %s at %d (given: %s)", token, index, given));
    }

    public static SelectorSyntaxErrorException unexpectedDeclare(String given, String declaring, int index)
    {
        return new SelectorSyntaxErrorException(String.format("Unexpected " + declaring + " declaration at %d (given: %s)", index, given));
    }

    public static SelectorSyntaxErrorException unexpectedEnd(String given, String ending, int index)
    {
        return new SelectorSyntaxErrorException(String.format("Unexpected " + ending + " ending at %d (given: %s)", index, given));
    }

    public static SelectorSyntaxErrorException reachedEnd(String given, String whileDoing, int index)
    {
        return new SelectorSyntaxErrorException(String.format("Reached end while " + whileDoing + " at %d (given: %s)", index, given));
    }
}
