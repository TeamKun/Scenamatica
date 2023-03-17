package net.kunmc.lab.scenamatica.scenario.beans.context;

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
    /**
     * ワールド名を定義します。
     *
     * @serialField name
     */
    String name;

    /**
     * ワールドの種類を定義します。
     *
     * @serialField type
     */
    WorldType type;

    /**
     * ワールドのシード値を定義します。
     *
     * @serialField seed
     */
    @Nullable
    Long seed;

    /**
     * 構造物を生成するかどうかを定義します。
     *
     * @serialField structures
     */
    @Nullable
    Boolean generateStructures;
    /**
     * ワールドの環境を定義します（e.g. ネザー、エンド）。
     *
     * @serialField env
     */
    @Nullable
    World.Environment environment;
    /**
     * ハードコアモードかどうかを定義します。
     *
     * @serialField hardcore
     */
    @Nullable
    Boolean hardcore;

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
        result.put("name", bean.name);

        // オプション項目
        MapUtils.putPrimitiveOrStrIfNotNull(result, "type", bean.type);
        MapUtils.putPrimitiveOrStrIfNotNull(result, "seed", bean.seed);
        MapUtils.putPrimitiveOrStrIfNotNull(result, "structures", bean.generateStructures);
        MapUtils.putPrimitiveOrStrIfNotNull(result, "env", bean.environment);
        MapUtils.putPrimitiveOrStrIfNotNull(result, "hardcore", bean.hardcore);

        return result;
    }
    public static void validateMap(Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, "name");
        MapUtils.checkType(map, "name", String.class);
        MapUtils.checkType(map, "seed", Number.class);
        MapUtils.checkType(map, "structures", Boolean.class);
        MapUtils.checkType(map, "env", String.class);
        MapUtils.checkType(map, "hardcore", Boolean.class);
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
                (String) map.get("name"),
                MapUtils.getAsEnumOrNull(map, "type", WorldType.class),
                MapUtils.getOrNull(map, "seed"),
                MapUtils.getOrNull(map, "structures"),
                MapUtils.getAsEnumOrNull(map, "env", World.Environment.class),
                MapUtils.getOrNull(map, "hardcore")
        );
    }

}
