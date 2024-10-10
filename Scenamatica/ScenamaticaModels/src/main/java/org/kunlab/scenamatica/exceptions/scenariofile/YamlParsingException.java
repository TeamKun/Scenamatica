package org.kunlab.scenamatica.exceptions.scenariofile;

import lombok.Getter;
import org.jetbrains.annotations.Nullable;

@Getter
public class YamlParsingException extends InvalidScenarioFileException
{
    private final int line;

    private final String[] aroundLines;  // 必ず奇数行。[0-n/2-1]が前方、[n/2]がエラー行、[n/2+1-n-1]が後方

    @Nullable
    private final String targetKey;

    public YamlParsingException(String message, String fileName, String targetKey, int line, String[] aroundLines, Throwable exception)
    {
        super(message, fileName, exception);

        this.targetKey = targetKey;
        this.line = line;
        this.aroundLines = aroundLines;
    }

    public YamlParsingException(String message, String fileName, String targetKey, int line, String[] aroundLines)
    {
        super(message, fileName);

        this.targetKey = targetKey;
        this.line = line;
        this.aroundLines = aroundLines;
    }


}
