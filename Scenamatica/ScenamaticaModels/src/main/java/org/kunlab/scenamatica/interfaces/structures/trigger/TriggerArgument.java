package org.kunlab.scenamatica.interfaces.structures.trigger;

import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

/**
 * トリガーの引数を表すインターフェースです。
 */
public interface TriggerArgument extends Structure
{
    /**
     * 引数が同じかどうかを比較します。与えられた引数と同じ場合は, トリガに紐づいたシナリオが実行されます。
     */
    boolean isSame(TriggerArgument argument);
}
