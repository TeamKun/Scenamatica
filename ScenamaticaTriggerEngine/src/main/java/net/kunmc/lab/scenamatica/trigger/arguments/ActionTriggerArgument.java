package net.kunmc.lab.scenamatica.trigger.arguments;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * アクショントリガーの引数を表すインターフェースです。
 */
@Value
public class ActionTriggerArgument implements TriggerArgument
{
    private static final String KEY_ACTION_TYPE = "action";
    private static final String KEY_ACTION_ARGS = "with";

    String actionType;
    Map<String, Object> actionArguments;

    public static Map<String, Object> serialize(ActionTriggerArgument argument)
    {
        Map<String, Object> result = new HashMap<>();
        result.put(KEY_ACTION_TYPE, argument.actionType.toLowerCase(Locale.ROOT));
        MapUtils.putIfNotNull(result, KEY_ACTION_ARGS, argument.actionArguments);

        return result;
    }

    public static void validate(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, KEY_ACTION_TYPE);
        if (map.containsKey(KEY_ACTION_ARGS))
            MapUtils.checkAndCastMap(map.get(KEY_ACTION_ARGS), String.class, Object.class);
    }

    public static ActionTriggerArgument deserialize(Map<String, Object> map)
    {
        validate(map);

        String type = map.get(KEY_ACTION_TYPE).toString().toLowerCase(Locale.ROOT);
        Map<String, Object> actionArguments;
        if (map.containsKey(KEY_ACTION_ARGS))
            actionArguments = MapUtils.checkAndCastMap(map.get(KEY_ACTION_ARGS), String.class, Object.class);
        else
            actionArguments = new HashMap<>();

        return new ActionTriggerArgument(
                type,
                actionArguments
        );
    }

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        return argument instanceof ActionTriggerArgument
                && Objects.equals(this.actionType, ((ActionTriggerArgument) argument).actionType)
                && this.actionArguments.equals(((ActionTriggerArgument) argument).actionArguments);
    }
}
