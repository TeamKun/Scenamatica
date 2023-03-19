package net.kunmc.lab.scenamatica.scenariofile.beans.trigger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ActionBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.interfaces.trigger.KeyedTriggerType;
import net.kunmc.lab.scenamatica.scenariofile.interfaces.trigger.TriggerArgument;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * シナリオのトリガーの種類を表す列挙型です。
 */
@Getter
@AllArgsConstructor
public enum TriggerType implements KeyedTriggerType
{
    MANUAL_DISPATCH("manual_dispatch", null),
    ON_ACTION("action", ActionBeanImpl.class),
    SCHEDULE("schedule", null),

    ;

    private static final String BEAN_SERIALIZER_METHOD = "serialize";
    private static final String BEAN_VALIDATOR_METHOD = "validate";
    private static final String BEAN_DESERIALIZER_METHOD = "deserialize";

    private final String key;
    @Nullable
    private final Class<? extends TriggerArgument> argumentType;

    public static TriggerType fromKey(String key)
    {
        for (TriggerType type : values())
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
