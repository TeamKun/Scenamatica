package org.kunlab.scenamatica.interfaces.action.input;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenario.SessionVariableHolder;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

/**
 * 入力値の参照を表すインターフェースです。
 *
 * @param <T> 入力値の型
 */
public interface InputReference<T>
{
    /**
     * 参照が同じかどうかを判定します。
     *
     * @param reference 参照
     * @return 同じ場合はtrue
     */
    boolean isEquals(String reference);

    /**
     * 参照を解決します。
     *
     * @param value 解決する値
     */
    void resolve(T value);

    /**
     * 参照を解決します。
     *
     * @param variables セッション変数
     * @throws IllegalStateException 解決できない場合
     */
    void resolve(@NotNull StructureSerializer serializer, @NotNull SessionVariableHolder variables);

    /**
     * この参照のトークンを取得します。
     *
     * @return トークン
     */
    @NotNull
    InputToken<T> getToken();

    /**
     * この参照の参照元を取得します。
     *
     * @return 参照元
     */
    Object getReferencing();

    /**
     * 値を取得します。
     * 解決済みであっても、値がnullの場合があります。
     *
     * @return 値
     */
    @Nullable
    T getValue();

    /**
     * この参照が解決済みかどうかを取得します。
     *
     * @return 解決済みの場合はtrue
     */
    boolean isResolved();
}
