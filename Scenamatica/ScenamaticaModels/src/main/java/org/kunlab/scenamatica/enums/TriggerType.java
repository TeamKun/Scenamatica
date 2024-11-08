package org.kunlab.scenamatica.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerArgument;

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

    private static final String STRUCTURE_SERIALIZER_METHOD = "serialize";
    private static final String STRUCTURE_VALIDATOR_METHOD = "validate";
    private static final String STRUCTURE_DESERIALIZER_METHOD = "deserialize";

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
                            STRUCTURE_SERIALIZER_METHOD, this.argumentType)
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
    public void validateArguments(StructuredYamlNode argument)
    {
        if (this.argumentType == null)
            return;

        try
        {
            this.argumentType.getMethod(STRUCTURE_VALIDATOR_METHOD, StructuredYamlNode.class)
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
     * @param node シリアライズされた引数
     * @return デシリアライズされた引数
     */
    public TriggerArgument deserialize(StructuredYamlNode node)
    {
        if (this.argumentType == null)
            return null;

        try
        {
            return (TriggerArgument) this.argumentType.getMethod(
                            STRUCTURE_DESERIALIZER_METHOD, StructuredYamlNode.class)
                    .invoke(null, node);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Failed to deserialize trigger argument.", e);
        }
    }
}
