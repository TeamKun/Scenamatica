package net.kunmc.lab.scenamatica.scenario.beans.context;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
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
@AllArgsConstructor
@RequiredArgsConstructor
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

    public WorldBean(String name)
    {
        this.name = name;
        this.type = WorldType.NORMAL;
        this.seed = null;
        this.generateStructures = null;
        this.environment = World.Environment.NORMAL;
        this.hardcore = false;
    }

}
