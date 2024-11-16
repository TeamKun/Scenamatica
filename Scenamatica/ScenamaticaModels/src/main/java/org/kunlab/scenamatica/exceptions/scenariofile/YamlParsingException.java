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
        final String separator = "-";
        final int separatorMax = "--------------------------------------------------".length();

        String messageHeaderFileName = "[" + this.getFileName() + "]";
        int headerSeparatorSize = (separatorMax - messageHeaderFileName.length()) / 2;
        String headerSeparator = StringUtils.repeat(separator, headerSeparatorSize)
                + messageHeaderFileName
                + StringUtils.repeat(separator, headerSeparatorSize);

        String shortMessage = createShortMessage();
        return shortMessage + "\n" +
                headerSeparator + "\n" +
                this.getAroundErrorLines() + "\n" +
                StringUtils.repeat(separator, separatorMax) + "\n";
    }

    private String createShortMessage()
    {
        String shortMessage = super.getMessage();
        if (this.getCause() == this || this.getCause() == null)
            return shortMessage;

        Throwable cause = this.getCause();
        String message;
        if (cause instanceof YamlParsingException)
            message = ((YamlParsingException) cause).getShortMessage();
        else
            message = cause.getMessage();
        shortMessage += " (caused by " + this.getCause().getClass().getSimpleName() + ": " + message + ")";

        return shortMessage;
    }

    private String getAroundErrorLines()
    {
        if (aroundLines == null)
            return "";

        StringBuilder sb = new StringBuilder();

        int targetLine = this.line + 1;  // 中央行を基準に設定
        int startLine = targetLine - (aroundLines.length - 1) / 2;
        int endLine = targetLine + (aroundLines.length - 1) / 2;

        int numPadding = String.valueOf(endLine).length();
        for (int i = 0; i < aroundLines.length; i++)
        {
            String line = aroundLines[i];
            int lineNum = startLine + i;
            String lineNumStr = String.format("%" + numPadding + "d", lineNum);

            sb.append(lineNumStr).append(": ").append(line).append("\n");

            // targetLine の行に HERE を表示
            if (lineNum == targetLine)
            {
                int cursor = this.targetKey == null ? 0: line.indexOf(this.targetKey);
                int paddingSize = numPadding + 1 + cursor - "[HERE]".length();
                sb.append("[HERE]")
                        .append(StringUtils.repeat("-", paddingSize))
                        .append("^").append("\n");
            }
        }

        return sb.toString();
    }
}
