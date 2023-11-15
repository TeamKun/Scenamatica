package org.kunlab.scenamatica.interfaces.scenariofile.trigger;

import org.kunlab.scenamatica.interfaces.scenariofile.Bean;

/**
 * トリガーの引数を表すインターフェースです。
 */
public interface TriggerArgument extends Bean
{
    /**
     * 引数が同じかどうかを比較します。与えられた引数と同じ場合は, トリガに紐づいたシナリオが実行されます。
     */
    boolean isSame(TriggerArgument argument);
}
