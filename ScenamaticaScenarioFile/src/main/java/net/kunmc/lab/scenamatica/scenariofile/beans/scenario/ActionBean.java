package net.kunmc.lab.scenamatica.scenariofile.beans.scenario;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.scenariofile.beans.trigger.TriggerArgument;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * シナリオの動作の定義を表すクラスです。
 */
@Value
public class ActionBean implements TriggerArgument
{
    public static final String KEY_TYPE = "action";
    public static final String KEY_ARGUMENTS = "with";

    /**
     * 動作の種類を定義します。
     */
    @NotNull
    String type;

    /**
     * 動作に必要な引数を定義します。
     */
    @Nullable
    Map<String, Object> arguments;

    /**
     * シナリオの動作の定義をMapにシリアライズします。
     *
     * @return シナリオの動作の定義をMapにシリアライズしたもの
     */
    public static Map<String, Object> serialize(ActionBean bean)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_TYPE, bean.getType());

        MapUtils.putIfNotNull(map, KEY_ARGUMENTS, bean.getArguments());

        return map;
    }

    /**
     * Mapがシリアライズされたシナリオの動作の定義であるかを検証します。
     *
     * @param map 検証するMap
     * @throws IllegalArgumentException Mapがシリアライズされたシナリオの動作の定義でない場合
     */
    public static void validate(Map<String, Object> map)
    {
        MapUtils.checkType(map, KEY_TYPE, String.class);

        if (map.containsKey(KEY_ARGUMENTS))
            MapUtils.checkAndCastMap(
                    map.get(KEY_ARGUMENTS),
                    String.class,
                    Object.class
            );

    }

    /**
     * シリアライズされたシナリオの動作の定義をデシリアライズします。
     *
     * @param map シリアライズされたシナリオの動作の定義
     * @return デシリアライズされたシナリオの動作の定義
     */
    public static ActionBean deserialize(Map<String, Object> map)
    {
        validate(map);

        String actionType = (String) map.get(KEY_TYPE);

        Map<String, Object> arguments = null;
        if (map.containsKey(KEY_ARGUMENTS))
            arguments = MapUtils.checkAndCastMap(
                    map.get(KEY_ARGUMENTS),
                    String.class,
                    Object.class
            );

        return new ActionBean(
                actionType,
                arguments
        );
    }

}
