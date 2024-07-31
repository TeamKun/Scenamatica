package org.kunlab.scenamatica.interfaces.context;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.context.stage.StageAlreadyDestroyedException;
import org.kunlab.scenamatica.exceptions.context.stage.StageCreateFailedException;
import org.kunlab.scenamatica.interfaces.structures.context.StageStructure;

/**
 * シナリオを実行するステージを管理するインターフェースです。
 */
public interface StageManager
{
    /**
     * ステージを生成します。
     *
     * @param structure        ステージ情報
     * @param timeoutMillis    タイムアウト時間, 0以下の場合は無制限
     * @param maxAttemptCounts 最大試行回数 0以下の場合は無制限
     * @return 生成したステージ
     */
    @NotNull Stage createStage(@NotNull StageStructure structure, long timeoutMillis, int maxAttemptCounts) throws StageCreateFailedException;

    /**
     * ステージをコピーします。
     *
     * @param originalName コピー元のワールド名
     * @return 生成したステージ
     */
    @NotNull Stage createStage(@Nullable String originalName) throws StageCreateFailedException;

    /**
     * 共有ステージを設定します。
     *
     * @param name 名前
     */
    @NotNull
    Stage shared(@NotNull String name);

    /**
     * ステージを破棄します。
     * 共有ステージの場合は破棄されません。
     */
    void destroyStage(@NotNull Stage stage) throws StageAlreadyDestroyedException;

    /**
     * ステージマネージャを破棄します。
     */
    void shutdown();
}
