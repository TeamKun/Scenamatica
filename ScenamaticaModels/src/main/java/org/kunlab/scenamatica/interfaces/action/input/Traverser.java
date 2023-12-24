package org.kunlab.scenamatica.interfaces.action.input;

/**
 * 入力値をトラバースするインターフェースです。
 *
 * @param <I> 入力値の型
 * @param <O> 出力値の型
 */
public interface Traverser<I, O>
{
    /**
     * トラバースします。
     *
     * @param obj 入力値
     * @return トラバース後の値
     */
    O traverse(I obj);

    /**
     * トラバースします。
     *
     * @param obj 入力値
     * @return トラバース後の値
     */
    O tryTraverse(Object obj);

    /**
     * トラバースする入力値の型を返します。
     *
     * @return 入力値の型
     */
    Class<? extends I> getInputClazz();

    /**
     * トラバースする関数型インターフェースを返します。
     *
     * @return 関数型インターフェース
     */
    InputTraverser<? super I, ? extends O> getTraverser();
}
