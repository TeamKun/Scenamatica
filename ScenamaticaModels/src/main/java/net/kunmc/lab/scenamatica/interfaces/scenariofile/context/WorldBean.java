package net.kunmc.lab.scenamatica.interfaces.scenariofile.context;

import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * ワールドの情報を表すインターフェースです。
 */
public interface WorldBean extends Serializable
{
    String KEY_NAME = "name";
    String KEY_TYPE = "type";
    String KEY_SEED = "seed";
    String KEY_GENERATE_STRUCTURES = "structures";
    String KEY_ENVIRONMENT = "env";
    String KEY_HARDCORE = "hardcore";

    /**
     * ワールド名を取得します。
     *
     * @return ワールド名
     */
    @NotNull
    String getName();

    /**
     * ワールドの種類を取得します。
     *
     * @return ワールドの種類
     */
    @NotNull
    WorldType getType();

    /**
     * ワールドのシード値を取得します。
     *
     * @return ワールドのシード値
     */
    @Nullable
    Long getSeed();

    /**
     * 構造物を生成するかどうかを取得します。
     *
     * @return 構造物を生成するかどうか
     */
    @Nullable
    Boolean getGenerateStructures();

    /**
     * ワールドの環境を取得します（e.g. ネザー、エンド）。
     *
     * @return ワールドの環境
     */
    @Nullable
    World.Environment getEnvironment();

    /**
     * ハードコアモードかどうかを取得します。
     *
     * @return ハードコアモードかどうか
     */
    boolean isHardcore();
}
