package net.kunmc.lab.scenamatica.scenariofile.interfaces.trigger;

import java.util.Map;

/**
 * トリガーのタイプを表す列挙型です。
 */
public interface KeyedTriggerType
{
    /**
     * トリガーの引数をシリアライズします。
     *
     * @param argument 引数
     * @return シリアライズされた引数
     */
    Map<String, Object> serializeArgument(TriggerArgument argument);

    /**
     * Mapが引数として正しいか検証します。
     *
     * @param argument 引数
     * @throws IllegalArgumentException 引数が不正な場合
     */
    void validateArguments(Map<String, Object> argument);

    /**
     * トリガーの引数をデシリアライズします。
     *
     * @param map シリアライズされた引数
     * @return デシリアライズされた引数
     */
    TriggerArgument deserialize(Map<String, Object> map);

    /**
     * トリガーのキーを取得します。
     *
     * @return キー
     */
    String getKey();

    Class<? extends TriggerArgument> getArgumentType();
}
