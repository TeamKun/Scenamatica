package org.kunlab.scenamatica.exceptions.scenariofile;

import lombok.Getter;
import org.apache.commons.lang.StringUtils;
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

    public String getShortMessage()
    {
        return super.getMessage();
    }

    @Override
    public String getMessage()
    {
        return super.getMessage() + "\n" + this.getAroundErrorLines();
    }

    private String getAroundErrorLines()
    {
        if (aroundLines == null)
            return "";

        StringBuilder sb = new StringBuilder();

        // aroundLine: [startLine, fuga, targetLine, piyo, endLine]

        int startLine = this.line;
        int targetLine = startLine + (aroundLines.length - 1) / 2;
        int endLine = startLine + aroundLines.length - 1;

        String targetLineContent = aroundLines[(aroundLines.length - 1) / 2];
        int cursor = this.targetKey == null ? 0: targetLineContent.indexOf(this.targetKey);

        int numPadding = String.valueOf(endLine).length();
        for (int i = 0; i < aroundLines.length; i++)
        {
            String line = aroundLines[i];
            int lineNum = startLine + i - (aroundLines.length - 1) / 2;
            String lineNumStr = String.format("%" + numPadding + "d", lineNum);

            sb.append(lineNumStr).append(": ").append(line).append("\n");

            if (lineNum == targetLine)
                sb.append(StringUtils.repeat("-", numPadding + 2 + cursor)).append("^ HERE!!!\n");
        }

        return sb.toString();
    }
}
