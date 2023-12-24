package org.kunlab.scenamatica.interfaces.action.input;

import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;

/**
 * 入力値保持を表すインターフェースです。
 *
 * @param <T> 入力値の型
 */
public interface InputValueHolder<T>
{
    /**
     * この入力値保持が指定されたトークンと等しいかどうかを返します。
     *
     * @param token 比較対象のトークン
     * @return 等しい場合はtrue
     */
    boolean isEquals(InputToken<?> token);

    /**
     * 値を返します。
     *
     * @return 値
     */
    @Nullable
    T getValue();

    /**
     * 値を設定します。
     *
     * @param obj 値
     */
    void set(@Nullable Object obj);

    /**
     * 値が存在するかどうかを返します。
     *
     * @return 存在する場合はtrue
     */
    boolean isPresent();

    /**
     * この入力値保持の値を検証します。
     *
     * @param type シナリオの種類
     */
    void validate(ScenarioType type);

    /**
     * この入力値保持のトークンを返します。
     *
     * @return トークン
     */
    InputToken<T> getToken();

    /**
     * この入力値保持の値参照を返します。
     *
     * @return 値参照
     */
    InputReference<T> getValueReference();
}
