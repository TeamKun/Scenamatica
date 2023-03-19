package net.kunmc.lab.scenamatica.scenariofile.beans.context;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * ワールドの情報を表すクラスです。
 */
@Value
public class WorldBean implements Serializable
{
    public static final String KEY_NAME = "name";
    public static final String KEY_TYPE = "type";
    public static final String KEY_SEED = "seed";
    public static final String KEY_GENERATE_STRUCTURES = "structures";
    public static final String KEY_ENVIRONMENT = "env";
    public static final String KEY_HARDCORE = "hardcore";

    /**
     * ワールド名を定義します。
     */
    String name;

    /**
     * ワールドの種類を定義します。
     */
    WorldType type;

    /**
     * ワールドのシード値を定義します。
     *
     */
    @Nullable
    Long seed;

    /**
     * 構造物を生成するかどうかを定義します。
     *
     */
    @Nullable
    Boolean generateStructures;
    /**
     * ワールドの環境を定義します（e.g. ネザー、エンド）。
     */
    @Nullable
    World.Environment environment;
    /**
     * ハードコアモードかどうかを定義します。
     */
    boolean hardcore;

    /**
     * ワールドの情報をMapにシリアライズします。
     * @param bean ワールドの情報
     * @return シリアライズされたMap
     */
    @NotNull
    public static Map<String, Object> serialize(WorldBean bean)
    {
        Map<String, Object> result = new HashMap<>();

        // 必須項目
        result.put(KEY_NAME, bean.name);

        // オプション項目
        MapUtils.putPrimitiveOrStrIfNotNull(result, KEY_TYPE, bean.type);
        MapUtils.putPrimitiveOrStrIfNotNull(result, KEY_SEED, bean.seed);
        MapUtils.putPrimitiveOrStrIfNotNull(result, KEY_GENERATE_STRUCTURES, bean.generateStructures);
        MapUtils.putPrimitiveOrStrIfNotNull(result, KEY_ENVIRONMENT, bean.environment);
        if (bean.hardcore)
            result.put(KEY_HARDCORE, true);

        return result;
    }

    /**
     * Mapがワールドの情報を表すMapかどうかを検証します。
     * @param map 検証するMap
     *
     * @throws IllegalArgumentException 必須項目が含まれていない場合か, 型が不正な場合
     */
    public static void validateMap(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, KEY_NAME);
        MapUtils.checkTypeIfContains(map, KEY_NAME, String.class);
        MapUtils.checkTypeIfContains(map, KEY_SEED, Number.class);
        MapUtils.checkTypeIfContains(map, KEY_GENERATE_STRUCTURES, Boolean.class);
        MapUtils.checkTypeIfContains(map, KEY_ENVIRONMENT, String.class);
        MapUtils.checkTypeIfContains(map, KEY_HARDCORE, Boolean.class);
    }

    /**
     * シリアライズされたMapからワールドの情報をデシリアライズします。
     * @param map シリアライズされたMap
     * @return ワールドの情報
     */
    @NotNull
    public static WorldBean deserialize(@NotNull Map<String, Object> map)
    {
        validateMap(map);

        return new WorldBean(
                (String) map.get(KEY_NAME),
                MapUtils.getAsEnumOrNull(map, KEY_TYPE, WorldType.class),
                MapUtils.getOrNull(map, KEY_SEED),
                MapUtils.getOrNull(map, KEY_GENERATE_STRUCTURES),
                MapUtils.getAsEnumOrNull(map, KEY_ENVIRONMENT, World.Environment.class),
                MapUtils.getOrDefault(map, KEY_HARDCORE, false)
        );
    }

}
