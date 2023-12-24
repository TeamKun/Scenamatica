package org.kunlab.scenamatica.interfaces.action.input;

import org.kunlab.scenamatica.enums.ScenarioType;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * 入力値を受け取るためのトークンです。
 *
 * @param <T> 入力値の型
 */
public interface InputToken<T>
{
    /**
     * このトークンの名前を取得します。
     *
     * @return トークン名
     */
    String getName();

    /**
     * このトークンの型を取得します。
     *
     * @return トークン型
     */
    Class<T> getClazz();

    /**
     * このトークンのデフォルト値を取得します。
     *
     * @return デフォルト値
     */
    T getDefaultValue();

    /**
     * 入力値のバリデタを登録します。
     *
     * @param type      シナリオタイプ
     * @param validator バリデタ
     * @return このトークン
     */
    InputToken<T> validator(ScenarioType type, Consumer<? super T> validator);

    /**
     * このトークンのバリデタを取得します。
     *
     * @return バリデタ
     */
    EnumMap<ScenarioType, Consumer<? super T>> getValidators();

    /**
     * 入力値のトラバーサを登録します。
     *
     * @param clazz     トラバースするオブジェクトの型
     * @param traverser トラバーサ
     * @param <I>       トラバースするオブジェクトの型
     * @return このトークン
     */
    <I> InputToken<T> traverser(Class<I> clazz, InputTraverser<? super I, ? extends T> traverser);

    /**
     * このトークンのトラバーサを取得します。
     *
     * @return トラバーサ
     */
    List<Traverser<?, T>> getTraversers();

    /**
     * 入力値を検証します。
     *
     * @param type  シナリオタイプ
     * @param value 入力値
     */
    void validate(ScenarioType type, T value);

    /**
     * 入力値をトラバースします。
     *
     * @param obj トラバースするオブジェクト
     * @return トラバース結果
     */
    T traverse(Object obj);
}
