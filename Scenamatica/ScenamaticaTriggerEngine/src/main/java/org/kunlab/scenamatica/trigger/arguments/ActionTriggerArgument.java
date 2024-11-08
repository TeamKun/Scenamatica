package org.kunlab.scenamatica.trigger.arguments;

import lombok.Value;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.scenario.ActionStructure;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerArgument;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * アクショントリガーの引数を表すインターフェースです。
 */
@Value
public class ActionTriggerArgument implements TriggerArgument, ActionStructure
{
    public static final String KEY_ACTION_TYPE = "action";
    public static final String KEY_ACTION_ARGS = "with";

    String type;
    StructuredYamlNode arguments;

    public static Map<String, Object> serialize(ActionTriggerArgument argument)
    {
        Map<String, Object> result = new HashMap<>();
        result.put(KEY_ACTION_TYPE, argument.type.toLowerCase(Locale.ROOT));
        if (argument.arguments != null)
        {
            try
            {
                result.put(KEY_ACTION_ARGS, argument.arguments.asMap());
            }
            catch (YamlParsingException ignored)
            {
            }
        }

        return result;
    }

    public static void validate(StructuredYamlNode node) throws YamlParsingException
    {
        node.get(KEY_ACTION_TYPE).ensureTypeOf(YAMLNodeType.STRING);
        node.get(KEY_ACTION_ARGS).ensureTypeOfIfExists(YAMLNodeType.MAPPING);
    }

    public static ActionTriggerArgument deserialize(StructuredYamlNode node) throws YamlParsingException
    {
        validate(node);

        String type = node.get(KEY_ACTION_TYPE).toString().toLowerCase(Locale.ROOT);
        StructuredYamlNode actionArguments = node.get(KEY_ACTION_ARGS);

        return new ActionTriggerArgument(
                type,
                actionArguments
        );
    }

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        return argument instanceof ActionTriggerArgument
                && Objects.equals(this.type, ((ActionTriggerArgument) argument).type)
                && this.arguments.equals(((ActionTriggerArgument) argument).arguments);
    }
}
