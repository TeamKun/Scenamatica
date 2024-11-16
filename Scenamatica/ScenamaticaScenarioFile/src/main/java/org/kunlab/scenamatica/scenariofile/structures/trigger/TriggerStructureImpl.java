package org.kunlab.scenamatica.scenariofile.structures.trigger;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.scenario.ActionStructure;
import org.kunlab.scenamatica.interfaces.structures.scenario.ScenarioStructure;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerArgument;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Value
public class TriggerStructureImpl implements TriggerStructure
{
    @NotNull
    TriggerType type;
    @Nullable
    TriggerArgument argument;
    @NotNull
    List<ScenarioStructure> beforeThat;
    @NotNull
    List<ScenarioStructure> afterThat;
    @Nullable
    ActionStructure runIf;

    @NotNull
    public static Map<String, Object> serialize(@NotNull TriggerStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = new HashMap<>();

        map.put(KEY_TYPE, structure.getType().getKey());

        if (structure.getArgument() != null)
        {
            Map<String, Object> arguments = structure.getType().serializeArgument(structure.getArgument());
            if (arguments != null)
                map.putAll(arguments);
        }

        if (!structure.getBeforeThat().isEmpty())
        {
            List<Map<String, Object>> list = new LinkedList<>();
            for (ScenarioStructure scenario : structure.getBeforeThat())
                list.add(serializer.serialize(scenario, ScenarioStructure.class));
            map.put(KEY_BEFORE_THAT, list);
        }
        if (!structure.getAfterThat().isEmpty())
        {
            List<Map<String, Object>> list = new LinkedList<>();
            for (ScenarioStructure scenario : structure.getAfterThat())
                list.add(serializer.serialize(scenario, ScenarioStructure.class));
            map.put(KEY_AFTER_THAT, list);
        }

        if (structure.getRunIf() != null)
            map.put(KEY_RUN_IF, serializer.serialize(structure.getRunIf(), ActionStructure.class));

        return map;
    }

    public static void validate(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        node.get(KEY_TYPE).ensureTypeOf(YAMLNodeType.STRING);
        node.get(KEY_TYPE).validate(n -> {
            if (TriggerType.fromKey(n.asString()) == null)
                throw new IllegalArgumentException("Invalid trigger type: " + n.asString());
            return null;
        });
        if (node.containsKey(KEY_BEFORE_THAT))
        {
            StructuredYamlNode beforeThatNode = node.get(KEY_BEFORE_THAT);
            beforeThatNode.ensureTypeOf(YAMLNodeType.LIST);
            for (StructuredYamlNode obj : beforeThatNode.asList())
                serializer.validate(obj, ScenarioStructure.class);
        }
        if (node.containsKey(KEY_AFTER_THAT))
        {
            StructuredYamlNode afterThatNode = node.get(KEY_AFTER_THAT);
            afterThatNode.ensureTypeOf(YAMLNodeType.LIST);
            for (StructuredYamlNode obj : afterThatNode.asList())
                serializer.validate(obj, ScenarioStructure.class);
        }

        TriggerType type = node.get(KEY_TYPE).getAs(n -> TriggerType.fromKey(n.asString()));
        if (type != null && type.getArgumentType() != null)
            type.validateArguments(node);

        if (node.containsKey(KEY_RUN_IF))
            serializer.validate(node.get(KEY_RUN_IF), ActionStructure.class);
    }

    @NotNull
    public static TriggerStructure deserialize(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        validate(node, serializer);

        TriggerType type = TriggerType.fromKey(node.get(KEY_TYPE).asString());

        TriggerArgument argument = null;
        if (type != null)
            argument = type.deserialize(node);

        List<ScenarioStructure> beforeThat = new LinkedList<>();
        if (node.containsKey(KEY_BEFORE_THAT))
            for (StructuredYamlNode obj : node.get(KEY_BEFORE_THAT).asList())
                beforeThat.add(serializer.deserialize(obj, ScenarioStructure.class));

        List<ScenarioStructure> afterThat = new LinkedList<>();
        if (node.containsKey(KEY_AFTER_THAT))
            for (StructuredYamlNode obj : node.get(KEY_AFTER_THAT).asList())
                afterThat.add(serializer.deserialize(obj, ScenarioStructure.class));

        ActionStructure runIf = null;
        if (node.containsKey(KEY_RUN_IF))
            runIf = serializer.deserialize(node.get(KEY_RUN_IF), ActionStructure.class);

        assert type != null;  // validate() で検証済み
        return new TriggerStructureImpl(
                type,
                argument,
                beforeThat,
                afterThat,
                runIf
        );
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof TriggerStructureImpl)) return false;
        TriggerStructureImpl that = (TriggerStructureImpl) o;
        return this.type == that.type;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.type);
    }
}
