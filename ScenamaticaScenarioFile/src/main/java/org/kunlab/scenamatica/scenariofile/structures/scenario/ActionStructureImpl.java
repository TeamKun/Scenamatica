package org.kunlab.scenamatica.scenariofile.structures.scenario;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;

import java.util.HashMap;
import java.util.Map;

@Value
public class ActionStructureImpl implements ActionStructure
{
    @NotNull
    String type;
    @Nullable
    Map<String, Object> arguments;

    @NotNull
    public static Map<String, Object> serialize(@NotNull ActionStructure structure)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_TYPE, structure.getType());

        MapUtils.putIfNotNull(map, KEY_ARGUMENTS, structure.getArguments());

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, KEY_TYPE);

        if (map.containsKey(KEY_ARGUMENTS) && map.get(KEY_ARGUMENTS) != null)
            MapUtils.checkAndCastMap(map.get(KEY_ARGUMENTS));

    }

    public static ActionStructure deserialize(Map<String, Object> map)
    {
        validate(map);

        String actionType = String.valueOf(map.get(KEY_TYPE));

        Map<String, Object> argumentsMap;
        if (map.containsKey(KEY_ARGUMENTS) && map.get(KEY_ARGUMENTS) != null)
            argumentsMap = MapUtils.checkAndCastMap(map.get(KEY_ARGUMENTS));
        else
            argumentsMap = null;

        return new ActionStructureImpl(
                actionType,
                argumentsMap
        );
    }

}
