package org.kunlab.scenamatica.scenariofile.structures.scenario;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.scenario.ActionStructure;
import org.kunlab.scenamatica.interfaces.structures.scenario.ScenarioStructure;
import org.kunlab.scenamatica.structures.StructureValidators;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Value
public class ScenarioStructureImpl implements ScenarioStructure
{
    private static final long DEFAULT_TIMEOUT_TICK = 20L * 5L;

    @NotNull
    ScenarioType type;
    @NotNull
    ActionStructure action;
    @Nullable
    String name;

    ActionStructure runIf;
    long timeout;  // Scenario は 他で使わないので NotNull(primitive).

    @NotNull
    public static Map<String, Object> serialize(@NotNull ScenarioStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_SCENARIO_TYPE, structure.getType().getKey());

        if (structure.getTimeout() != -1)
            map.put(KEY_TIMEOUT, structure.getTimeout());
        MapUtils.putIfNotNull(map, KEY_SCENARIO_NAME, structure.getName());

        map.putAll(serializer.serialize(structure.getAction(), ActionStructure.class));

        if (structure.getRunIf() != null)
            map.put(KEY_RUN_IF, serializer.serialize(structure.getRunIf(), ActionStructure.class));

        return map;
    }

    public static void validate(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        node.get(KEY_SCENARIO_TYPE).validateIfExists(StructureValidators.enumName(ScenarioType.class));
        node.get(KEY_TIMEOUT).ensureTypeOfIfExists(YAMLNodeType.NUMBER);
        node.get(KEY_SCENARIO_NAME).ensureTypeOfIfExists(YAMLNodeType.STRING);

        // scenario の名前が "scenario" になっている場合は, 予約語としてエラーを出す
        node.get(KEY_SCENARIO_NAME).validateIfExists(n -> {
            if (Objects.equals(n.get(KEY_SCENARIO_NAME).asString(), "scenario"))
                throw new IllegalArgumentException("scenario name \"scenario\" is reserved");
            return null;
        });

        serializer.validate(node, ActionStructure.class);
    }

    @NotNull
    public static ScenarioStructure deserialize(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        validate(node, serializer);

        ScenarioType type = node.get(KEY_SCENARIO_TYPE).getAs(n -> ScenarioType.fromKey(n.asString()));
        long timeout = node.get(KEY_TIMEOUT).asLong(DEFAULT_TIMEOUT_TICK);
        String name = node.get(KEY_SCENARIO_NAME).asString(null);

        ActionStructure runIf = null;
        if (node.containsKey(KEY_RUN_IF))
            runIf = serializer.deserialize(node.get(KEY_RUN_IF), ActionStructure.class);

        assert type != null;
        return new ScenarioStructureImpl(
                type,
                serializer.deserialize(node, ActionStructure.class),
                name,
                runIf,
                timeout
        );
    }
}
