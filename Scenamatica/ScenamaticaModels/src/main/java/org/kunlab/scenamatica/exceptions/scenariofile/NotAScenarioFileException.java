package org.kunlab.scenamatica.exceptions.scenariofile;

/**
 * シナリオファイルではないファイルを読み込んだ場合にスローされる例外です。
 */
public class NotAScenarioFileException extends InvalidScenarioFileException
{
    public NotAScenarioFileException(String fileName)
    {
        super("The file is not a scenario file(missing 'scenamatica' property?)", fileName);
    }
}
