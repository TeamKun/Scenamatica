package org.kunlab.scenamatica.exceptions.scenariofile;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

/**
 * 無効なシナリオファイルを読み込んだ場合にスローされる例外です。
 */
public class InvalidScenarioFileException extends Exception
{
    @Getter
    @Nullable
    private final String fileName;

    public InvalidScenarioFileException(String message, String fileName, Throwable exception)
    {
        super(message, exception);

        this.fileName = fileName;
    }

    public InvalidScenarioFileException(String message, String fileName)
    {
        super(message);
        this.fileName = fileName;
    }
}
