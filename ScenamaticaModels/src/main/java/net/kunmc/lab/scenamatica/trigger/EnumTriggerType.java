package net.kunmc.lab.scenamatica.trigger;

import lombok.Getter;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerType;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * シナリオのトリガーの種類を表す列挙型です。
 */
@Getter
public enum EnumTriggerType implements TriggerType
{
    MANUAL_DISPATCH("manual_dispatch"),
    ON_ACTION("action"),
    SCHEDULE("schedule"),

    ;

    private static final String BEAN_SERIALIZER_METHOD = "serialize";
    private static final String BEAN_VALIDATOR_METHOD = "validate";
    private static final String BEAN_DESERIALIZER_METHOD = "deserialize";

    private final String key;
    @Nullable
    private Class<? extends TriggerArgument> argumentType;

    EnumTriggerType(String key)
    {
        this.key = key;
    }

    public void setArgumentType(Class<? extends TriggerArgument> argumentType)
    {
        if (this.argumentType != null)
            throw new IllegalStateException("Argument type is already set.");

        this.argumentType = argumentType;
    }

    public static EnumTriggerType fromKey(String key)
    {
        for (EnumTriggerType type : values())
            if (type.getKey().equals(key))
                return type;

        return null;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> serializeArgument(TriggerArgument argument)
    {
        if (this.argumentType == null)
            return null;

        try
        {
            return (Map<String, Object>) this.argumentType.getMethod(
                            BEAN_SERIALIZER_METHOD, this.argumentType.getInterfaces()[0])
                    .invoke(null, argument);
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException("Failed to serialize trigger argument.", e);
        }
    }

    @Override
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

    @Override
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
