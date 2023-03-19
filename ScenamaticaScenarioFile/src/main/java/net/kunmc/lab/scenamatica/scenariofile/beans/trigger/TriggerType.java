package net.kunmc.lab.scenamatica.scenariofile.beans.trigger;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ActionBean;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * シナリオのトリガーの種類を表す列挙型です。
 */
@Getter
@AllArgsConstructor
public enum TriggerType
{
    MANUAL_DISPATCH("manual_dispatch", null),
    ON_ACTION("action", ActionBean.class),
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
