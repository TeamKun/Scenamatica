package org.kunlab.scenamatica.scenariofile.beans.scenario;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;

import java.util.HashMap;
import java.util.Map;

@Value
public class ScenarioBeanImpl implements ScenarioBean
{
    public static final String KEY_SCENARIO_TYPE = "type";
    public static final String KEY_RUN_IF = "runif";
    public static final String KEY_TIMEOUT = "timeout";
    private static final long DEFAULT_TIMEOUT_TICK = 20L * 5L;
    @NotNull
    ScenarioType type;
    @NotNull
    ActionBean action;

    ActionBean runIf;
    long timeout;  // Scenario は 他で使わないので NotNull(primitive).

    /**
     * シナリオをMapにシリアライズします。
     *
     * @return シナリオをMapにシリアライズしたもの
     */
    @NotNull
    public static Map<String, Object> serialize(@NotNull ScenarioBean bean, @NotNull BeanSerializer serializer)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_SCENARIO_TYPE, bean.getType().getKey());

        if (bean.getTimeout() != -1)
            map.put(KEY_TIMEOUT, bean.getTimeout());

        map.putAll(serializer.serialize(bean.getAction(), ActionBean.class));

        if (bean.getRunIf() != null)
            map.put(KEY_RUN_IF, serializer.serialize(bean.getRunIf(), ActionBean.class));

        return map;
    }

    /**
     * Mapがシリアライズされたシナリオであるかを検証します。
     *
     * @param map        検証する Map
     * @param serializer シリアライザ
     * @throws IllegalArgumentException Mapがシリアライズされたシナリオでない場合
     */
    public static void validate(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        MapUtils.checkType(map, KEY_SCENARIO_TYPE, String.class);
        if (ScenarioType.fromKey((String) map.get(KEY_SCENARIO_TYPE)) == null)
            throw new IllegalArgumentException("Invalid scenario type");

        MapUtils.checkNumberIfContains(map, KEY_TIMEOUT);

        serializer.validate(map, ActionBean.class);
    }

    /**
     * シリアライズされたシナリオをデシリアライズします。
     *
     * @param map シリアライズされたシナリオ
     * @return デシリアライズされたシナリオ
     */
    @NotNull
    public static ScenarioBean deserialize(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        validate(map, serializer);

        ScenarioType type = ScenarioType.fromKey((String) map.get(KEY_SCENARIO_TYPE));
        long timeout = MapUtils.getAsLongOrDefault(map, KEY_TIMEOUT, DEFAULT_TIMEOUT_TICK);

        ActionBean runIf = null;
        if (map.containsKey(KEY_RUN_IF))
            runIf = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_RUN_IF)),
                    ActionBean.class
            );

        assert type != null;
        return new ScenarioBeanImpl(
                type,
                serializer.deserialize(map, ActionBean.class),
                runIf,
                timeout
        );
    }
}
