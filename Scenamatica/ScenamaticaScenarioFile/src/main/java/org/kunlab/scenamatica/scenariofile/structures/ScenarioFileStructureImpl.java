package org.kunlab.scenamatica.scenariofile.structures;

import lombok.Value;
import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioOrder;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.scenariofile.VersionRange;
import org.kunlab.scenamatica.interfaces.structures.context.ContextStructure;
import org.kunlab.scenamatica.interfaces.structures.scenario.ActionStructure;
import org.kunlab.scenamatica.interfaces.structures.scenario.ScenarioStructure;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;
import org.kunlab.scenamatica.structures.StructureMappers;
import org.kunlab.scenamatica.structures.StructureValidators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Value
public class ScenarioFileStructureImpl implements ScenarioFileStructure
{
    @NotNull
    Version scenamaticaVersion;
    @Nullable
    VersionRange minecraftVersions;
    @NotNull
    String name;
    @Nullable
    String description;
    long timeout;
    int order;
    @NotNull
    List<TriggerStructure> triggers;
    @Nullable
    ActionStructure runIf;
    @Nullable
    ContextStructure context;
    @NotNull
    List<ScenarioStructure> scenario;

    @NotNull
    public static Map<String, Object> serialize(@NotNull ScenarioFileStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<String, Object> map = new HashMap<>();

        map.put(KEY_SCENAMATICA_VERSION, structure.getScenamaticaVersion().toString());

        if (structure.getMinecraftVersions() != null)
            map.put(KEY_MINECRAFT_VERSIONS, serializer.serialize(structure.getMinecraftVersions(), VersionRange.class));

        map.put(KEY_NAME, structure.getName());
        MapUtils.putIfNotNull(map, KEY_DESCRIPTION, structure.getDescription());

        if (structure.getTimeout() != DEFAULT_TIMEOUT_TICK)
            map.put(KEY_TIMEOUT, structure.getTimeout());
        if (structure.getOrder() != ScenarioOrder.NORMAL.getOrder())
            map.put(KEY_ORDER, structure.getOrder());

        map.put(KEY_TRIGGERS, structure.getTriggers().stream()
                .map(trigger -> serializer.serialize(trigger, TriggerStructure.class))
                .collect(Collectors.toList())
        );
        map.put(KEY_SCENARIO, structure.getScenario().stream()
                .map(scenario -> serializer.serialize(scenario, ScenarioStructure.class))
                .collect(Collectors.toList())
        );

        if (structure.getContext() != null)
            map.put(KEY_CONTEXT, serializer.serialize(structure.getContext(), ContextStructure.class));
        if (structure.getRunIf() != null)
            map.put(KEY_RUN_IF, serializer.serialize(structure.getRunIf(), ActionStructure.class));

        return map;
    }

    public static void validate(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        StructuredYamlNode scenamaticaVersionNode = node.get(KEY_SCENAMATICA_VERSION);
        if (!Version.isValidVersionString(scenamaticaVersionNode.asString()))
            throw new IllegalArgumentException("Invalid version string: " + scenamaticaVersionNode);

        if (node.containsKey(KEY_MINECRAFT_VERSIONS))
            serializer.validate(node.get(KEY_MINECRAFT_VERSIONS), VersionRange.class);

        node.get(KEY_NAME).ensureTypeOf(YAMLNodeType.STRING);
        node.get(KEY_DESCRIPTION).ensureTypeOfIfExists(YAMLNodeType.STRING);
        node.get(KEY_TIMEOUT).ensureTypeOfIfExists(YAMLNodeType.NUMBER);
        node.get(KEY_ORDER).ensureTypeOfIfExists(YAMLNodeType.NUMBER);

        if (node.containsKey(KEY_CONTEXT))
            serializer.validate(node.get(KEY_CONTEXT), ContextStructure.class);

        node.get(KEY_TRIGGERS).validateIfExists(StructureValidators.listType(serializer, TriggerStructure.class));
        node.get(KEY_SCENARIO).validateIfExists(StructureValidators.listType(serializer, ScenarioStructure.class));
    }

    @NotNull
    public static ScenarioFileStructure deserialize(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        validate(node, serializer);

        Version scenamatica = Version.of(node.get(KEY_SCENAMATICA_VERSION).asString());
        VersionRange minecraftVersions = node.get(KEY_MINECRAFT_VERSIONS).getAs(
                n -> serializer.deserialize(n, VersionRange.class),
                null
        );
        String name = node.get(KEY_NAME).asString();
        String description = node.get(KEY_DESCRIPTION).asString();
        long timeout = node.get(KEY_TIMEOUT).asLong(DEFAULT_TIMEOUT_TICK);

        int order = ScenarioOrder.NORMAL.getOrder();
        if (node.containsKey(KEY_ORDER))
        {
            StructuredYamlNode orderObject = node.get(KEY_ORDER);

            String orderString = String.valueOf(orderObject);
            ScenarioOrder enumOrder;
            if ((enumOrder = ScenarioOrder.of(orderString)) == null)
                order = orderObject.asInteger();
            else
                order = enumOrder.getOrder();
        }

        List<TriggerStructure> triggers = node.get(KEY_TRIGGERS).getAs(
                StructureMappers.deserializedList(serializer, TriggerStructure.class),
                Collections.emptyList()
        );

        List<ScenarioStructure> scenario = node.get(KEY_SCENARIO).getAs(
                StructureMappers.deserializedList(serializer, ScenarioStructure.class),
                new ArrayList<>()
        );

        ContextStructure context = null;
        if (node.containsKey(KEY_CONTEXT))
            context = serializer.deserialize(node.get(KEY_CONTEXT), ContextStructure.class);

        ActionStructure runIf = null;

        if (node.containsKey(KEY_RUN_IF))
            runIf = serializer.deserialize(node.get(KEY_RUN_IF), ActionStructure.class);
        return new ScenarioFileStructureImpl(
                scenamatica,
                minecraftVersions,
                name,
                description,
                timeout,
                order,
                triggers,
                runIf,
                context,
                scenario
        );
    }
}
