package org.kunlab.scenamatica.interfaces.action;

import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

/**
 * 動作の引数のインタフェースです。
 */
public interface ActionArgument extends TriggerArgument
{
    /**
     * 引数の文字列を返します。
     *
     * @return 引数の文字列
     */
    String getArgumentString();

}
