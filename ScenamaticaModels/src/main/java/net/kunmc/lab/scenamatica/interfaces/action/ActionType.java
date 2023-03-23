package net.kunmc.lab.scenamatica.interfaces.action;

import java.util.Map;

/**
 * アクションのタイプを表す列挙型です。
 */
public interface ActionType
{
    /**
     * アクションの引数をシリアライズします。
     *
     * @param argument 引数
     * @return シリアライズされた引数
     */
    Map<String, Object> serializeArgument(ActionArgument argument);

    /**
     * Mapが引数として正しいか検証します。
     *
     * @param argument 引数
     * @throws IllegalArgumentException 引数が不正な場合
     */
    void validateArguments(Map<String, Object> argument);

    /**
     * アクションの引数をデシリアライズします。
     *
     * @param map シリアライズされた引数
     * @return デシリアライズされた引数
     */
    ActionArgument deserialize(Map<String, Object> map);

    /**
     * アクションのキーを取得します。
     *
     * @return キー
     */
    String getKey();

    Class<? extends ActionArgument> getArgumentType();
}
