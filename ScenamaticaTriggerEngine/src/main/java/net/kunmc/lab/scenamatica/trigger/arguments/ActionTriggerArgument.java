package net.kunmc.lab.scenamatica.trigger.arguments;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.enums.ActionType;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * {@link net.kunmc.lab.scenamatica.enums.TriggerType#ON_ACTION} トリガーの引数を表すインターフェースです。
 */
@Value
public class ActionTriggerArgument implements TriggerArgument
{
    private static final String KEY_ACTION_TYPE = "action";
    private static final String KEY_ACTION_ARGS = "with";

    ActionType actionType;
    Map<String, Object> actionArguments;

    public static Map<String, Object> serialize(ActionTriggerArgument argument)
    {
        Map<String, Object> result = new HashMap<>();
        result.put(KEY_ACTION_TYPE, argument.actionType.name().toLowerCase(Locale.ROOT));
        MapUtils.putIfNotNull(result, KEY_ACTION_ARGS, argument.actionArguments);

        return result;
    }

    public static void validate(Map<String, Object> map)
    {
        MapUtils.checkEnumName(map, KEY_ACTION_TYPE, ActionType.class);
        if (map.containsKey(KEY_ACTION_ARGS))
            MapUtils.checkAndCastMap(map.get(KEY_ACTION_ARGS), String.class, Object.class);
    }

    public static ActionTriggerArgument deserialize(Map<String, Object> map)
    {
        validate(map);

        ActionType type = MapUtils.getAsEnum(map, KEY_ACTION_TYPE, ActionType.class);
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
                && this.actionType == ((ActionTriggerArgument) argument).actionType
                && this.actionArguments.equals(((ActionTriggerArgument) argument).actionArguments);
    }
}
