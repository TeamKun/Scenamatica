package net.kunmc.lab.scenamatica.action;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionType;

import java.util.Map;

@Getter
@RequiredArgsConstructor
public enum EnumActionType implements ActionType
{
    NONE("none");

    private static final String BEAN_SERIALIZER_METHOD = "serialize";
    private static final String BEAN_VALIDATOR_METHOD = "validate";
    private static final String BEAN_DESERIALIZER_METHOD = "deserialize";

    private final String key;

    private Class<ActionArgument> argumentType;

    public void setArgumentType(Class<ActionArgument> argumentType)
    {
        if (this.argumentType != null)
            throw new IllegalStateException("Argument type is already set.");

        this.argumentType = argumentType;
    }

    @Override
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
