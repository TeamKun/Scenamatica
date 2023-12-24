package org.kunlab.scenamatica.interfaces.action.input;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenario.SessionVariableHolder;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Map;

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
     * @param serializer
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
     * トークンを取得します。
     *
     * @param tokens トークン
     * @return トークン
     */
    InputBoard oneOf(InputToken<?>... tokens);

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
