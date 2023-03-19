package net.kunmc.lab.scenamatica.scenariofile.beans.trigger;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ActionBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ScenarioBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * シナリオがトリガーされるタイミングを表すクラスです。
 */
@Value
public class TriggerBean implements Serializable
{
    private static final String KEY_TYPE = "type";
    private static final String KEY_BEFORE_THAT = "before";
    private static final String KEY_AFTER_THAT = "after";

    /**
     * トリガーの種類です。殆どの場合は {@link TriggerType#ON_ACTION} です。
     */
    @NotNull
    TriggerType type;

    /**
     * 種類が {@link TriggerType#ON_ACTION} だった場合に, 動作を格納します。
     */
    @Nullable
    ActionBean action;

    /**
     * 本シナリオを実行する前に実行するシナリオを格納します。
     */
    @NotNull
    List<ScenarioBean> beforeThat;

    /**
     * 本シナリオを実行した後に実行するシナリオを格納します。
     */
    @NotNull
    List<ScenarioBean> afterThat;

    /**
     * シナリオのトリガーを Map にシリアライズします。*
     *
     * @param bean シナリオのトリガーの Bean
     * @return シリアライズされた Map
     */
    @NotNull
    public static Map<String, Object> serialize(@NotNull TriggerBean bean)
    {
        Map<String, Object> map = new HashMap<>();

        map.put(KEY_TYPE, bean.type.getKey());

        if (bean.action != null)
            map.putAll(ActionBean.serialize(bean.action));
        if (!bean.beforeThat.isEmpty())
        {
            List<Map<String, Object>> list = new LinkedList<>();
            for (ScenarioBean scenario : bean.beforeThat)
                list.add(ScenarioBean.serialize(scenario));
            map.put(KEY_BEFORE_THAT, list);
        }
        if (!bean.afterThat.isEmpty())
        {
            List<Map<String, Object>> list = new LinkedList<>();
            for (ScenarioBean scenario : bean.afterThat)
                list.add(ScenarioBean.serialize(scenario));
            map.put(KEY_AFTER_THAT, list);
        }

        return map;
    }

    /**
     * Map がシリアライズされたシナリオのトリガーであるかを検証します。
     *
     * @param map 検証する Map
     * @throws IllegalArgumentException 検証に失敗した場合
     */
    public static void validateMap(@NotNull Map<String, Object> map)
    {
        MapUtils.checkType(map, KEY_TYPE, String.class);
        if (TriggerType.fromKey((String) map.get(KEY_TYPE)) == null)
            throw new IllegalArgumentException("Invalid trigger type: " + map.get(KEY_TYPE));

        if (map.containsKey(KEY_BEFORE_THAT))
        {
            MapUtils.checkType(map, KEY_BEFORE_THAT, List.class);
            for (Object obj : (List<?>) map.get(KEY_BEFORE_THAT))
                ScenarioBean.validateMap(MapUtils.checkAndCastMap(
                        obj,
                        String.class,
                        Object.class
                ));
        }
        if (map.containsKey(KEY_AFTER_THAT))
        {
            MapUtils.checkType(map, KEY_AFTER_THAT, List.class);
            for (Object obj : (List<?>) map.get(KEY_AFTER_THAT))
                ScenarioBean.validateMap(MapUtils.checkAndCastMap(
                        obj,
                        String.class,
                        Object.class
                ));
        }

        ActionBean.validateMap(map);
    }

    /**
     * シナリオのトリガーを Map からデシリアライズします。
     *
     * @param map シリアライズされた Map
     * @return デシリアライズされたシナリオのトリガー
     */
    @NotNull
    public static TriggerBean deserialize(@NotNull Map<String, Object> map)
    {
        validateMap(map);

        TriggerType type = TriggerType.fromKey((String) map.get(KEY_TYPE));

        ActionBean action = null;
        if (type == TriggerType.ON_ACTION)
            action = ActionBean.deserialize(map);

        List<ScenarioBean> beforeThat = new LinkedList<>();
        if (map.containsKey(KEY_BEFORE_THAT))
            for (Object obj : (List<?>) map.get(KEY_BEFORE_THAT))
                beforeThat.add(ScenarioBean.deserialize(MapUtils.checkAndCastMap(
                        obj,
                        String.class,
                        Object.class
                )));

        List<ScenarioBean> afterThat = new LinkedList<>();
        if (map.containsKey(KEY_AFTER_THAT))
            for (Object obj : (List<?>) map.get(KEY_AFTER_THAT))
                afterThat.add(ScenarioBean.deserialize(MapUtils.checkAndCastMap(
                        obj,
                        String.class,
                        Object.class
                )));

        assert type != null;  // validate() で検証済み
        return new TriggerBean(
                type,
                action,
                beforeThat,
                afterThat
        );
    }
}
