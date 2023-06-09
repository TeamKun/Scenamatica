package org.kunlab.scenamatica.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * シナリオのトリガの種類を表す列挙型です。
 */
@Getter
@RequiredArgsConstructor
public enum TriggerType
{
    MANUAL_DISPATCH("manual_dispatch"),
    ON_ACTION("on_action"),
    ON_LOAD("on_load"),
    SCHEDULE("schedule"),

    ;

    private static final String BEAN_SERIALIZER_METHOD = "serialize";
    private static final String BEAN_VALIDATOR_METHOD = "validate";
    private static final String BEAN_DESERIALIZER_METHOD = "deserialize";

    /**
     * トリガのキーです。
     */
    private final String key;
    /**
     * トリガの引数の型です。
     */
    @Nullable
    private Class<? extends TriggerArgument> argumentType;

    public static TriggerType fromKey(String key)
    {
        for (TriggerType type : values())
            if (type.getKey().equals(key))
                return type;

        return null;
    }

    public void setArgumentType(Class<? extends TriggerArgument> argumentType)
    {
        if (this.argumentType != null)
            throw new IllegalStateException("Argument type is already set.");

        this.argumentType = argumentType;
    }

    /**
     * トリガの引数をシリアライズします。
     *
     * @param argument 引数
     * @return シリアライズされた引数
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> serializeArgument(TriggerArgument argument)
    {
        if (this.argumentType == null)
            return null;

        try
        {
            return (Map<String, Object>) this.argumentType.getMethod(
                            BEAN_SERIALIZER_METHOD, this.argumentType)
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
     * トリガの引数をデシリアライズします。
     *
     * @param map シリアライズされた引数
     * @return デシリアライズされた引数
     */
    public TriggerArgument deserialize(Map<String, Object> map)
    {
        if (this.argumentType == null)
            return null;

        try
        {
            return (TriggerArgument) this.argumentType.getMethod(
                            BEAN_DESERIALIZER_METHOD, Map.class)
                    .invoke(null, map);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Failed to deserialize trigger argument.", e);
        }
    }
}
