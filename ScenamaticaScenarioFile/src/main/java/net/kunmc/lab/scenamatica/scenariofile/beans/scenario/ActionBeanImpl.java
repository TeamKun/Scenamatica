package net.kunmc.lab.scenamatica.scenariofile.beans.scenario;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Value
public class ActionBeanImpl implements ActionBean
{
    @NotNull
    String type;
    @Nullable
    Map<String, Object> arguments;

    @NotNull
    public static Map<String, Object> serialize(@NotNull ActionBean bean)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_TYPE, bean.getType());

        MapUtils.putIfNotNull(map, KEY_ARGUMENTS, bean.getArguments());

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, KEY_TYPE);

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

        String actionType = String.valueOf(map.get(KEY_TYPE));

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
