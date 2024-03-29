package org.kunlab.scenamatica.exceptions.scenariofile;

/**
 * 無効なシナリオファイルを読み込んだ場合にスローされる例外です。
 */
public class InvalidScenarioFileException extends Exception
{

    public InvalidScenarioFileException(String message, IllegalArgumentException e)
    {
        super("Invalid scenario file syntax: " + message, e);
    }

    public InvalidScenarioFileException(String message)
    {
        super(message);
    }
}
