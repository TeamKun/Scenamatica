package net.kunmc.lab.scenamatica.interfaces.scenariofile.misc;

import org.bukkit.Material;
import org.bukkit.block.Biome;

import java.util.Map;

/**
 * ブロックの情報を格納するクラスです。
 */
public interface BlockBean
{
    String KEY_BLOCK_TYPE = "type";
    String KEY_BLOCK_X = "x";
    String KEY_BLOCK_Y = "y";
    String KEY_BLOCK_Z = "z";
    String KEY_METADATA = "metadata";
    String KEY_LIGHT_LEVEL = "light";
    String KEY_BIOME = "biome";

    /**
     * ブロックの種類を取得します。
     *
     * @return ブロックの種類
     */
    Material getType();

    /**
     * ブロックのX座標を取得します。
     *
     * @return ブロックのX座標
     */
    int getX();

    /**
     * ブロックのY座標を取得します。
     *
     * @return ブロックのY座標
     */
    int getY();

    /**
     * ブロックのZ座標を取得します。
     *
     * @return ブロックのZ座標
     */
    int getZ();

    /**
     * ブロックのメタデータを取得します。
     *
     * @return ブロックのメタデータ
     */
    Map<String, Object> getMetadata();

    /**
     * ブロックの明るさを取得します。
     *
     * @return ブロックの明るさ
     */
    byte getLightLevel();

    /**
     * ブロックのバイオームを取得します。
     *
     * @return ブロックのバイオーム
     */
    Biome getBiome();
}
