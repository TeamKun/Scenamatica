package org.kunlab.scenamatica.scenariofile.structures.scenario;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioStructure;

import java.util.HashMap;
import java.util.Map;

@Value
public class ScenarioStructureImpl implements ScenarioStructure
{
    public static final String KEY_SCENARIO_TYPE = "type";
    public static final String KEY_SCENARIO_NAME = "name";
    public static final String KEY_RUN_IF = "runif";
    public static final String KEY_TIMEOUT = "timeout";

    private static final long DEFAULT_TIMEOUT_TICK = 20L * 5L;

    @NotNull
    ScenarioType type;
    @NotNull
    ActionStructure action;
    @Nullable
    String name;

    ActionStructure runIf;
    long timeout;  // Scenario は 他で使わないので NotNull(primitive).

    /**
     * シナリオをMapにシリアライズします。
     *
     * @return シナリオをMapにシリアライズしたもの
     */
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

    /**
     * Mapがシリアライズされたシナリオであるかを検証します。
     *
     * @param map        検証する Map
     * @param serializer シリアライザ
     * @throws IllegalArgumentException Mapがシリアライズされたシナリオでない場合
     */
    public static void validate(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        MapUtils.checkType(map, KEY_SCENARIO_TYPE, String.class);
        if (ScenarioType.fromKey((String) map.get(KEY_SCENARIO_TYPE)) == null)
            throw new IllegalArgumentException("Invalid scenario type");

        MapUtils.checkNumberIfContains(map, KEY_TIMEOUT);

        serializer.validate(map, ActionStructure.class);
    }

    /**
     * シリアライズされたシナリオをデシリアライズします。
     *
     * @param map シリアライズされたシナリオ
     * @return デシリアライズされたシナリオ
     */
    @NotNull
    public static ScenarioStructure deserialize(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validate(map, serializer);

        ScenarioType type = ScenarioType.fromKey((String) map.get(KEY_SCENARIO_TYPE));
        long timeout = MapUtils.getAsLongOrDefault(map, KEY_TIMEOUT, DEFAULT_TIMEOUT_TICK);
        String name = MapUtils.getOrNull(map, KEY_SCENARIO_NAME);

        ActionStructure runIf = null;
        if (map.containsKey(KEY_RUN_IF))
            runIf = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_RUN_IF)),
                    ActionStructure.class
            );

        assert type != null;
        return new ScenarioStructureImpl(
                type,
                serializer.deserialize(map, ActionStructure.class),
                name,
                runIf,
                timeout
        );
    }
}
