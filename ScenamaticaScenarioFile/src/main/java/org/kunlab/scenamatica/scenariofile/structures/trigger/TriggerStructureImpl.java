package org.kunlab.scenamatica.scenariofile.structures.trigger;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerStructure;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Value
public class TriggerStructureImpl implements TriggerStructure
{
    public static final String KEY_TYPE = "type";
    public static final String KEY_BEFORE_THAT = "before";
    public static final String KEY_AFTER_THAT = "after";
    public static final String KEY_RUN_IF = "runif";

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

    public static void validate(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        MapUtils.checkType(map, KEY_TYPE, String.class);
        if (TriggerType.fromKey((String) map.get(KEY_TYPE)) == null)
            throw new IllegalArgumentException("Invalid trigger type: " + map.get(KEY_TYPE));

        if (map.containsKey(KEY_BEFORE_THAT))
        {
            MapUtils.checkType(map, KEY_BEFORE_THAT, List.class);
            for (Object obj : (List<?>) map.get(KEY_BEFORE_THAT))
                serializer.validate(
                        MapUtils.checkAndCastMap(obj),
                        ScenarioStructure.class
                );
        }
        if (map.containsKey(KEY_AFTER_THAT))
        {
            MapUtils.checkType(map, KEY_AFTER_THAT, List.class);
            for (Object obj : (List<?>) map.get(KEY_AFTER_THAT))
                serializer.validate(
                        MapUtils.checkAndCastMap(obj),
                        ScenarioStructure.class
                );
        }

        TriggerType type = TriggerType.fromKey((String) map.get(KEY_TYPE));
        if (type != null && type.getArgumentType() != null)
            type.validateArguments(map);

        if (map.containsKey(KEY_RUN_IF))
            MapUtils.checkType(map, KEY_RUN_IF, Map.class);
    }

    @NotNull
    public static TriggerStructure deserialize(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validate(map, serializer);

        TriggerType type = TriggerType.fromKey((String) map.get(KEY_TYPE));

        TriggerArgument argument = null;
        if (type != null)
            argument = type.deserialize(map);

        List<ScenarioStructure> beforeThat = new LinkedList<>();
        if (map.containsKey(KEY_BEFORE_THAT))
            for (Object obj : (List<?>) map.get(KEY_BEFORE_THAT))
                beforeThat.add(serializer.deserialize(
                        MapUtils.checkAndCastMap(obj), ScenarioStructure.class));

        List<ScenarioStructure> afterThat = new LinkedList<>();
        if (map.containsKey(KEY_AFTER_THAT))
            for (Object obj : (List<?>) map.get(KEY_AFTER_THAT))
                afterThat.add(serializer.deserialize(
                        MapUtils.checkAndCastMap(obj), ScenarioStructure.class));

        ActionStructure runIf = null;
        if (map.containsKey(KEY_RUN_IF))
            runIf = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_RUN_IF)), ActionStructure.class);

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
