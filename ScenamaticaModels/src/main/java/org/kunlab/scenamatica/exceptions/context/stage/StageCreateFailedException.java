package org.kunlab.scenamatica.exceptions.context.stage;

import lombok.Getter;
import org.kunlab.scenamatica.exceptions.context.ContextPreparationException;
import org.jetbrains.annotations.NotNull;

/**
 * ステージの作成に失敗したことを表す例外です。
 */
@Getter
public class StageCreateFailedException extends ContextPreparationException
{
    @NotNull
    private final String stageName;

    public StageCreateFailedException(@NotNull String stageName)
    {
        super("Failed to create stage: " + stageName);
        this.stageName = stageName;
    }

    public StageCreateFailedException(@NotNull String stageName, Throwable cause)
    {
        super("Failed to create stage: " + stageName, cause);
        this.stageName = stageName;
    }
}
