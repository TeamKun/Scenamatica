package net.kunmc.lab.scenamatica.scenariofile.interfaces.scenario;

import net.kunmc.lab.scenamatica.scenariofile.interfaces.trigger.TriggerArgument;

import java.util.Map;

/**
 * シナリオの動作の定義を表すインターフェースです。
 */
public interface ActionBean extends TriggerArgument
{
    String KEY_TYPE = "action";
    String KEY_ARGUMENTS = "with";

    /**
     * 動作の種類を定義します。
     *
     * @return 動作の種類
     */
    String getType();

    /**
     * 動作に必要な引数を定義します。
     *
     * @return 動作に必要な引数
     */
    Map<String, Object> getArguments();
}
