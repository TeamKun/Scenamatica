package net.kunmc.lab.scenamatica.scenariofile.beans.scenario;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Value
public class ScenarioBeanImpl implements ScenarioBean
{
    private static final String KEY_SCENARIO_TYPE = "type";
    private static final String KEY_RUN_IF = "runif";

    @NotNull
    ScenarioType type;
    @NotNull
    ActionBean action;
    @Nullable
    ActionBean runIf;
    long timeout;

    /**
     * シナリオをMapにシリアライズします。
     *
     * @return シナリオをMapにシリアライズしたもの
     */
    public static Map<String, Object> serialize(ScenarioBean bean)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_SCENARIO_TYPE, bean.getType().getKey());

        if (bean.getTimeout() != -1)
            map.put(KEY_TIMEOUT, bean.getTimeout());

        map.putAll(ActionBeanImpl.serialize(bean.getAction()));

        if (bean.getRunIf() != null)
            map.put(KEY_RUN_IF, ActionBeanImpl.serialize(bean.getRunIf()));

        return map;
    }

    /**
     * Mapがシリアライズされたシナリオであるかを検証します。
     *
     * @param map 検証するMap
     * @throws IllegalArgumentException Mapがシリアライズされたシナリオでない場合
     */
    public static void validate(Map<String, Object> map)
    {
        MapUtils.checkType(map, KEY_SCENARIO_TYPE, String.class);
        if (ScenarioType.fromKey((String) map.get(KEY_SCENARIO_TYPE)) == null)
            throw new IllegalArgumentException("Invalid scenario type");

        MapUtils.checkTypeIfContains(map, KEY_TIMEOUT, Long.class);

        ActionBeanImpl.validate(map);
    }

    /**
     * シリアライズされたシナリオをデシリアライズします。
     *
     * @param map シリアライズされたシナリオ
     * @return デシリアライズされたシナリオ
     */
    public static ScenarioBean deserialize(Map<String, Object> map)
    {
        validate(map);

        ScenarioType type = ScenarioType.fromKey((String) map.get(KEY_SCENARIO_TYPE));
        long timeout = MapUtils.getAsLongOrDefault(map, KEY_TIMEOUT, -1L);

        ActionBean runIf = null;
        if (map.containsKey(KEY_RUN_IF))
            runIf = ActionBeanImpl.deserialize(MapUtils.checkAndCastMap(
                    map.get(KEY_RUN_IF),
                    String.class,
                    Object.class
            ));

        assert type != null;
        return new ScenarioBeanImpl(
                type,
                ActionBeanImpl.deserialize(map),
                runIf,
                timeout
        );
    }
}
