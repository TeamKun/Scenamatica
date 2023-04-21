package net.kunmc.lab.scenamatica.interfaces.action;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

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
