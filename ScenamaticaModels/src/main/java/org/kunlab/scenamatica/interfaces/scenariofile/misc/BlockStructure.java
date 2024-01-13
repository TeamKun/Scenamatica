package org.kunlab.scenamatica.interfaces.scenariofile.misc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.Creatable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

import java.util.Map;

/**
 * ブロックの情報を格納するクラスです。
 */
public interface BlockStructure extends Structure, Mapped<Block>, Creatable<Block>, ProjectileSourceStructure
{
    String KEY_BLOCK_TYPE = "type";
    String KEY_BLOCK_LOCATION = "location";
    String KEY_METADATA = "metadata";
    String KEY_LIGHT_LEVEL = "light";
    String KEY_BIOME = "biome";
    String KEY_BLOCK_DATA = "blockData";
    String KEY_BLOCK_STATE = "blockState";

    /**
     * ブロックの種類を取得します。
     *
     * @return ブロックの種類
     */
    Material getType();

    /**
     * ブロックの場所を取得します。
     *
     * @return ブロックの場所
     */
    LocationStructure getLocation();

    /**
     * ブロックのメタデータを取得します。
     *
     * @return ブロックのメタデータ
     */
    @NotNull
    Map<String, Object> getMetadata();

    /**
     * ブロックの明るさを取得します。
     *
     * @return ブロックの明るさ
     */
    Integer getLightLevel();

    /**
     * ブロックのバイオームを取得します。
     *
     * @return ブロックのバイオーム
     */
    Biome getBiome();

    /**
     * ブロックのデータを取得します。
     *
     * @return ブロックのデータ
     */
    @NotNull
    Map<String, Object> getBlockData();

    /**
     * ブロックの状態を取得します。
     *
     * @return ブロックの状態
     */
    Byte getBlockState();

    /**
     * 安全にブロックを取得します。
     *
     * @return ブロック
     */
    Block getBlockSafe();

    /**
     * Location にこのブロックの情報を適用します。
     *
     * @param location 適用先の Location
     * @return 適用後の Block
     */
    Block apply(@NotNull Location location);

    /**
     * このブロックの情報を適用します。
     *
     * @return 適用後の Block
     */
    Block apply();

    /**
     * ワールド情報が欠落している場合に, ScenarioEngine からワールド情報を取得してから Location にこのブロックの情報を適用します。
     *
     * @param engine   取得元の ScenarioEngine
     * @param location 適用先の Location
     * @return 適用後の Location
     */
    Block apply(@NotNull ScenarioEngine engine, @Nullable Location location);
}
