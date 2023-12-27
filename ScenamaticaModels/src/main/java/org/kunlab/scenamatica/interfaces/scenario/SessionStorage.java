package org.kunlab.scenamatica.interfaces.scenario;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * セッション変数を表すインターフェースです。
 */
public interface SessionStorage
{
    /**
     * キーから値を取得します。
     *
     * @param key キー
     * @return 値
     * @throws IllegalArgumentException キーが存在しない場合
     */
    @Nullable
    Object get(@NotNull String key);

    /**
     * キーに値を設定します。
     *
     * @param key   キー
     * @param value 値
     */
    void set(@NotNull String key, @Nullable Object value);
}
