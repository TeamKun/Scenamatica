package org.kunlab.scenamatica.interfaces.action.input;

import org.kunlab.scenamatica.exceptions.scenariofile.InvalidScenarioFileException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

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
        return (ser, input) -> (R) input;
    }

    /**
     * トラバースします。
     *
     * @throws InvalidScenarioFileException トラバース中に例外が発生した場合
     * @return トラバース後の値
     */
    O traverse(StructureSerializer ser, I obj) throws InvalidScenarioFileException;
}
