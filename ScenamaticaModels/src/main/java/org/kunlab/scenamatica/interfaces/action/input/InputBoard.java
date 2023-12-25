package org.kunlab.scenamatica.interfaces.action.input;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenario.SessionVariableHolder;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * アクションの入力を表すクラスです。
 */
public interface InputBoard extends TriggerArgument
{

    /**
     * 入力をコンパイルします。
     *
     * @param map 対象のマップ
     */
    void compile(@NotNull StructureSerializer serializer, @NotNull Map<String, Object> map);

    /**
     * 入力値にある参照を解決します。
     *
     * @param serializer シリアライザー
     * @param variables  変数のホルダー
     */
    void resolveReferences(@NotNull StructureSerializer serializer, @NotNull SessionVariableHolder variables);

    /**
     * 未解決の参照があるかどうかを返します。
     *
     * @return 未解決の参照があるかどうか
     */
    boolean hasUnresolvedReferences();

    /**
     * トークンが含まれているかどうかを返します。
     *
     * @param token トークン
     * @return 含まれているかどうか
     */
    boolean contains(InputToken<?> token);

    /**
     * oneOf コントラクトを追加します。
     *
     * @param tokens トークン
     * @return this
     */
    InputBoard oneOf(InputToken<?>... tokens);

    /**
     * required コントラクトを追加します。
     *
     * @param tokens トークン
     * @return this
     */
    InputBoard requireNonNull(InputToken<?>... tokens);

    /**
     * 入力を追加します。
     *
     * @param token トークン
     * @return this
     */
    InputBoard register(InputToken<?> token);

    /**
     * 入力を追加します。
     *
     * @param token トークン
     * @return this
     */
    InputBoard registerAll(InputToken<?>... token);

    /**
     * バリデタを追加します。
     *
     * @param validator             バリデタ
     * @param validateFailedMessage バリデーションエラー時のメッセージ
     * @return this
     */
    InputBoard validator(@NotNull Predicate<? super InputBoard> validator, @Nullable String validateFailedMessage);

    /**
     * 値を取得します。
     *
     * @param token トークン
     * @param <T>   値の型
     * @return 値
     */
    <T> InputValueHolder<T> getHolder(InputToken<T> token);

    /**
     * 値を取得します。
     *
     * @param token トークン
     * @param <T>   値の型
     * @return 値
     */
    <T> T get(InputToken<T> token);

    /**
     * 値があるかどうかを返します。
     *
     * @param token トークン
     * @return 値があるかどうか
     */
    boolean has(@NotNull InputToken<?> token);

    /**
     * 値が解決済みかどうかを返します。
     *
     * @param token トークン
     * @return 値が解決済みかどうか
     */
    boolean isResolved(@NotNull InputToken<?> token);

    /**
     * 値が解決されている場合に、指定された関数を適用します。
     *
     * @param mapper       適用する関数
     * @param defaultValue 値が null の場合に返す値
     * @param token        トークン
     * @param <T>          値の型
     * @param <U>          適用結果の型
     * @return 適用結果
     */
    <T, U> U ifResolved(@NotNull InputToken<T> token, @NotNull Function<? super InputValueHolder<T>, ? extends U> mapper, @Nullable U defaultValue);

    /**
     * 値が解決されている場合に、指定された Predicate を適用します。
     *
     * @param predicate    適用する Predicate
     * @param defaultValue 値が null の場合に返す値
     * @param token        トークン
     * @param <T>          値の型
     * @return 適用結果
     */
    <T> boolean ifResolved(@NotNull InputToken<T> token, @NotNull Predicate<? super InputValueHolder<T>> predicate, boolean defaultValue);

    /**
     * 値が解決されている場合に、指定された Predicate を適用します。
     *
     * @param predicate 適用する Predicate
     * @param token     トークン
     * @param <T>       値の型
     * @return 適用結果
     */
    <T> boolean isResolved(@NotNull InputToken<T> token, @NotNull Predicate<? super InputValueHolder<T>> predicate);

    /**
     * 値が解決済みで、かつ null でないかどうかを返します。
     *
     * @param token トークン
     * @return 値が解決済みで、かつ null でないかどうか
     */
    boolean isPresent(@NotNull InputToken<?> token);

    /**
     * 値が解決されていて、かつ null でない場合に、指定された関数を適用します。
     *
     * @param mapper       適用する関数
     * @param defaultValue 値が null の場合に返す値
     * @param token        トークン
     * @param <T>          値の型
     * @param <U>          適用結果の型
     * @return 適用結果
     */
    <T, U> U ifPresent(@NotNull InputToken<T> token, @NotNull Function<? super T, ? extends U> mapper, @Nullable U defaultValue);

    /**
     * 値が解決されていて、かつ null でない場合に、指定された関数を適用します。
     *
     * @param predicate    適用する Predicate
     * @param defaultValue 値が null の場合に返す値
     * @param token        トークン
     * @param <T>          値の型
     * @return 適用結果
     */
    <T> boolean ifPresent(@NotNull InputToken<T> token, @NotNull Predicate<? super T> predicate, boolean defaultValue);

    /**
     * 値が解決されていて、かつ null でない場合に、指定された関数を適用します。
     * null の場合は true を返します。
     *
     * @param predicate 適用する Predicate
     * @param token     トークン
     * @param <T>       値の型
     * @return 適用結果
     */
    <T> boolean ifPresent(@NotNull InputToken<T> token, @NotNull Predicate<? super T> predicate);

    /**
     * 値が解決されていて、かつ null でない場合に、指定された関数を適用します。
     * null の場合は true を返します。
     *
     * @param predicate 適用する Predicate
     * @param token     トークン
     * @param <T>       値の型
     * @return 適用結果
     */
    <T> boolean runIfPresent(@NotNull InputToken<T> token, @NotNull Consumer<? super T> predicate);

    /**
     * 値を検証します。
     */
    void validate();

    /**
     * 値を文字列として取得します。
     *
     * @return 値
     */
    String getValuesString();
}
