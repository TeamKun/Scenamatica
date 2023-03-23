package net.kunmc.lab.scenamatica.scenariofile.beans.scenario;

import lombok.Value;
import net.kunmc.lab.scenamatica.action.ActionType;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Value
public class ActionBeanImpl implements ActionBean
{
    @NotNull
    ActionType type;
    @Nullable
    ActionArgument argument;

    public static Map<String, Object> serialize(ActionBean bean)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_TYPE, bean.getType().getKey());

        MapUtils.putIfNotNull(map, KEY_ARGUMENTS, bean.getArgument());

        return map;
    }

    public static void validate(Map<String, Object> map)
    {
        MapUtils.checkEnumName(map, KEY_TYPE, ActionType.class);

        if (map.containsKey(KEY_ARGUMENTS))
            MapUtils.checkAndCastMap(
                    map.get(KEY_ARGUMENTS),
                    String.class,
                    Object.class
            );

    }

    public static ActionBean deserialize(Map<String, Object> map)
    {
        validate(map);

        ActionType actionType = MapUtils.getAsEnum(
                map,
                KEY_TYPE,
                ActionType.class
        );

        ActionArgument argument;
        if (map.containsKey(KEY_ARGUMENTS))
        {
            Map<String, Object> argumentsMap = MapUtils.checkAndCastMap(
                    map.get(KEY_ARGUMENTS),
                    String.class,
                    Object.class
            );

            argument = actionType.deserialize(argumentsMap);
        }
        else
            argument = null;

        return new ActionBeanImpl(
                actionType,
                argument
        );
    }

}
