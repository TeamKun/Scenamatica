package org.kunlab.scenamatica.interfaces.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ActionResultCause;

import java.util.Map;
import java.util.UUID;

/**
 * アクションの実行結果を表すインターフェースです。
 */
public interface ActionResult
{
    /**
     * アクションの名前を取得します。
     *
     * @return アクションの名前
     */
    String getActionName();

    /**
     * アクションの実行時のコンテキストを取得します。
     *
     * @return アクションの実行時のコンテキスト
     */
    UUID getRunID();

    /**
     * アクションが成功したかどうかを取得します。
     *
     * @return アクションが成功したかどうか
     */
    boolean isSuccess();

    /**
     * シナリオの実行を中断したかどうかを取得します。
     *
     * @return シナリオの実行を中断したかどうか
     */
    boolean isHalt();

    /**
     * アクションの実行結果の原因を取得します。
     *
     * @return アクションの実行結果の原因
     */
    ActionResultCause getCause();

    /**
     * 発声したエラーを取得します。
     *
     * @return 発生したエラー
     */
    @Nullable
    Throwable getError();

    /**
     * アクションの実行結果の出力を取得します。
     *
     * @return アクションの実行結果の出力
     */
    @NotNull
    Map<String, Object> getOutputs();
}
