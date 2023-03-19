package net.kunmc.lab.scenamatica.scenariofile;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.scenariofile.beans.context.ContextBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ScenarioBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.trigger.TriggerBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * シナリオのファイルの情報を保持するクラスです。
 */
@Value
public class ScenarioFileBean implements Serializable
{
    private static final String KEY_NAME = "name";
    private static final String KEY_TRIGGERS = "on";
    private static final String KEY_CONTEXT = "context";
    private static final String KEY_SCENARIO = "scenario";

    /**
     * シナリオの名前です。
     * 人間でも読みやすい名前が望ましいです。
     */
    @NotNull
    String name;

    /**
     * シナリオのトリガを定義します。
     */
    @NotNull
    List<TriggerBean> triggers;

    /**
     * シナリオの実行に必要な情報を定義します。
     */
    @Nullable
    ContextBean context;

    /**
     * シナリオを定義します。
     */
    @NotNull
    List<ScenarioBean> scenario;

    /**
     * シナリオのファイルの情報を Map にシリアライズします。
     *
     * @param bean シナリオのファイルの情報の Bean
     * @return シリアライズされた Map
     */
    @NotNull
    public static Map<String, Object> serialize(@NotNull ScenarioFileBean bean)
    {
        Map<String, Object> map = new HashMap<>();

        map.put(KEY_NAME, bean.name);
        map.put(KEY_TRIGGERS, bean.triggers.stream()
                .map(TriggerBean::serialize)
                .collect(Collectors.toList())
        );
        map.put(KEY_SCENARIO, bean.scenario.stream()
                .map(ScenarioBean::serialize)
                .collect(Collectors.toList())
        );

        if (bean.context != null)
            map.put(KEY_CONTEXT, ContextBean.serialize(bean.context));

        return map;
    }

    /**
     * Map がシナリオのファイルの情報を表しているかを検証します。
     *
     * @param map 検証する Map
     * @throws IllegalArgumentException 検証に失敗した場合
     */
    public static void validateMap(@NotNull Map<String, Object> map)
    {
        MapUtils.checkType(map, KEY_NAME, String.class);

        if (map.containsKey(KEY_CONTEXT))
            ContextBean.validateMap(MapUtils.checkAndCastMap(
                    map.get(KEY_CONTEXT),
                    String.class,
                    Object.class
            ));

        MapUtils.checkType(map, KEY_TRIGGERS, List.class);
        ((List<?>) map.get(KEY_TRIGGERS)).forEach(o -> TriggerBean.validateMap(MapUtils.checkAndCastMap(
                o,
                String.class,
                Object.class
        )));

        MapUtils.checkType(map, KEY_SCENARIO, List.class);
        ((List<?>) map.get(KEY_SCENARIO)).forEach(o -> ScenarioBean.validateMap(MapUtils.checkAndCastMap(
                o,
                String.class,
                Object.class
        )));
    }

    /**
     * シリアライズされた Map からシナリオのファイルの情報をデシリアライズします。
     *
     * @param map シリアライズされた Map
     * @return デシリアライズされたシナリオのファイルの情報の Bean
     */
    @NotNull
    public static ScenarioFileBean deserialize(@NotNull Map<String, Object> map)
    {
        validateMap(map);

        String name = (String) map.get(KEY_NAME);
        List<TriggerBean> triggers = ((List<?>) map.get(KEY_TRIGGERS)).stream()
                .map(o -> TriggerBean.deserialize(MapUtils.checkAndCastMap(
                        o,
                        String.class,
                        Object.class
                )))
                .collect(Collectors.toList());
        List<ScenarioBean> scenario = ((List<?>) map.get(KEY_SCENARIO)).stream()
                .map(o -> ScenarioBean.deserialize(MapUtils.checkAndCastMap(
                        o,
                        String.class,
                        Object.class
                )))
                .collect(Collectors.toList());

        ContextBean context = null;
        if (map.containsKey(KEY_CONTEXT))
            context = ContextBean.deserialize(MapUtils.checkAndCastMap(
                    map.get(KEY_CONTEXT),
                    String.class,
                    Object.class
            ));

        return new ScenarioFileBean(name, triggers, context, scenario);
    }
}
