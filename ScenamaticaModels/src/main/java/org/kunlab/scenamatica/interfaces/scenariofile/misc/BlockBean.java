package org.kunlab.scenamatica.interfaces.scenariofile.misc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * ブロックの情報を格納するクラスです。
 */
public interface BlockBean
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
    Location getLocation();

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
}
