package org.kunlab.scenamatica.interfaces.action.input;

/**
 * 入力値をトラバースする関数型インターフェースです。
 *
 * @param <I> 入力値の型
 * @param <O> 出力値の型
 */
@FunctionalInterface
public interface InputTraverser<I, O>
{
    static <T, R> InputTraverser<T, R> casted()
    {
        // noinspection unchecked
        return (input) -> (R) input;
    }

    /**
     * トラバースします。
     *
     * @return トラバース後の値
     */
    O traverse(I obj);
}
