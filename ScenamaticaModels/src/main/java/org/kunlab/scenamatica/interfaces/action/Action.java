package org.kunlab.scenamatica.interfaces.action;

import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;

/**
 * 動作のインタフェースです。
 */
public interface Action
{
    /**
     * 動作のシリアライズ名を返します。
     *
     * @return 動作のシリアライズ名
     */
    String getName();

    /**
     * 入力値の構造を返します。
     *
     * @param type シナリオの種類
     * @return 入力値の構造
     */
    InputBoard getInputBoard(ScenarioType type);
}
