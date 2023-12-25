package org.kunlab.scenamatica.interfaces.action.input;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

import java.util.function.Function;
import java.util.function.Predicate;

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
     * 値が解決済みで、かつ null であるかどうかを返します。
     *
     * @return null である場合は true
     */
    boolean isNull();

    /**
     * 値が解決済みで、かつ null でないかどうかを返します。
     *
     * @return null でない場合は true
     */
    boolean isNotNull();

    /**
     * 値が null でない場合に、指定された関数を適用します。
     *
     * @param mapper       適用する関数
     * @param defaultValue 値が null の場合に返す値
     * @param <U>          適用結果の型
     * @return 適用結果
     */
    <U> U ifNotNull(@NotNull Function<? super T, ? extends U> mapper, @Nullable U defaultValue);

    /**
     * 値が null でない場合に、指定された Predicate を適用します。
     *
     * @param predicate    適用する Predicate
     * @param defaultValue 値が null の場合に返す値
     * @return 適用結果
     */
    boolean ifNotNull(@NotNull Predicate<? super T> predicate, boolean defaultValue);

    /**
     * 値が null でない場合に、指定された Predicate を適用します。
     * null の場合は false を返します。
     *
     * @param predicate 適用する Predicate
     * @return 適用結果
     */
    boolean ifNotNull(@NotNull Predicate<? super T> predicate);

    /**
     * 値を設定します。
     *
     * @param obj 値
     */
    void set(@NotNull StructureSerializer serializer, @Nullable Object obj);

    /**
     * 値を空に設定します。
     */
    void setEmpty();

    /**
     * 値を空かどうかを返します。
     */
    boolean isEmpty();

    /**
     * 値が解決されているかどうかを返します。
     *
     * @return 解決されている場合はtrue
     */
    boolean isResolved();

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
