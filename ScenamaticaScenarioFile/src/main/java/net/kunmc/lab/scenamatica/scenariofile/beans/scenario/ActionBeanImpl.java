package net.kunmc.lab.scenamatica.scenariofile.beans.scenario;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.enums.ActionType;
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
    Map<String, Object> arguments;

    public static Map<String, Object> serialize(ActionBean bean)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_TYPE, bean.getType().getKey());

        MapUtils.putIfNotNull(map, KEY_ARGUMENTS, bean.getArguments());

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

        Map<String, Object> argumentsMap;
        if (map.containsKey(KEY_ARGUMENTS))
            argumentsMap = MapUtils.checkAndCastMap(
                    map.get(KEY_ARGUMENTS),
                    String.class,
                    Object.class
            );
        else
            argumentsMap = null;

        return new ActionBeanImpl(
                actionType,
                argumentsMap
        );
    }

}
