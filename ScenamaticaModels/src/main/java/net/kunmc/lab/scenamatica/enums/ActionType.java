package net.kunmc.lab.scenamatica.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;

import java.util.Map;

/**
 * アクションのタイプを表す列挙型です。
 */
@Getter
@RequiredArgsConstructor
public enum ActionType
{
    NONE("none");

    private static final String BEAN_SERIALIZER_METHOD = "serialize";
    private static final String BEAN_VALIDATOR_METHOD = "validate";
    private static final String BEAN_DESERIALIZER_METHOD = "deserialize";

    /**
     * アクションのキーです。
     */
    private final String key;

    private Class<ActionArgument> argumentType;

    public void setArgumentType(Class<ActionArgument> argumentType)
    {
        if (this.argumentType != null)
            throw new IllegalStateException("Argument type is already set.");

        this.argumentType = argumentType;
    }

    /**
     * アクションの引数をシリアライズします。
     *
     * @param argument 引数
     * @return シリアライズされた引数
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> serializeArgument(ActionArgument argument)
    {
        if (this.argumentType == null)
            return null;

        try
        {
            return (Map<String, Object>) this.argumentType.getMethod(BEAN_SERIALIZER_METHOD, this.argumentType.getInterfaces()[0])
                    .invoke(null, argument);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Failed to serialize trigger argument.", e);
        }
    }

    /**
     * Mapが引数として正しいか検証します。
     *
     * @param argument 引数
     * @throws IllegalArgumentException 引数が不正な場合
     */
    public void validateArguments(Map<String, Object> argument)
    {
        if (this.argumentType == null)
            return;

        try
        {
            this.argumentType.getMethod(BEAN_VALIDATOR_METHOD, Map.class)
                    .invoke(null, argument);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Failed to validate trigger argument.", e);
        }
    }

    /**
     * アクションの引数をデシリアライズします。
     *
     * @param map シリアライズされた引数
     * @return デシリアライズされた引数
     */
    public ActionArgument deserialize(Map<String, Object> map)
    {
        if (this.argumentType == null)
            return null;

        try
        {
            return (ActionArgument) this.argumentType.getMethod(BEAN_DESERIALIZER_METHOD, Map.class)
                    .invoke(null, map);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Failed to deserialize trigger argument.", e);
        }
    }
}
