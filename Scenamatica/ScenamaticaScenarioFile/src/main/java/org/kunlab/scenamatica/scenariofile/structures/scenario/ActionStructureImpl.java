package org.kunlab.scenamatica.scenariofile.structures.scenario;

import lombok.SneakyThrows;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.scenario.ActionStructure;

import java.util.HashMap;
import java.util.Map;

@Value
public class ActionStructureImpl implements ActionStructure
{
    @NotNull
    String type;
    @NotNull
    StructuredYamlNode arguments;

    @NotNull
    @SneakyThrows(YamlParsingException.class)
    public static Map<String, Object> serialize(@NotNull ActionStructure structure)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_TYPE, structure.getType());

        if (!structure.getArguments().isNullish())
            map.put(KEY_ARGUMENTS, structure.getArguments().asMap());

        return map;
    }

    public static void validate(@NotNull StructuredYamlNode node) throws YamlParsingException
    {
        node.get(KEY_TYPE).ensureTypeOfIfExists(YAMLNodeType.STRING);
        node.get(KEY_ARGUMENTS).ensureTypeOfIfExists(
                YAMLNodeType.MAPPING,
                YAMLNodeType.NULL  // からの場合は null となる
        );
    }

    public static ActionStructure deserialize(StructuredYamlNode node) throws YamlParsingException
    {
        validate(node);

        String actionType = node.get(KEY_TYPE).asString();
        StructuredYamlNode argumentsMap = node.get(KEY_ARGUMENTS);

        return new ActionStructureImpl(
                actionType,
                argumentsMap
        );
    }

}
