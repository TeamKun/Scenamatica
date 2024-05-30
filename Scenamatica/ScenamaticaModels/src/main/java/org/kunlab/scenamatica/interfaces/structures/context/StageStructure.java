package org.kunlab.scenamatica.interfaces.structures.context;

import org.bukkit.World;
import org.bukkit.WorldType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

/**
 * ワールドの情報を表すインターフェースです。
 */
@TypeDoc(
        name = "Stage",
        description = "ステージの情報を表します。",
        mappingOf = World.class,
        properties = {
                @TypeProperty(
                        name = "copyOf",
                        type = String.class,
                        description = "オリジナルのワールド名です。"
                ),
                @TypeProperty(
                        name = "type",
                        type = WorldType.class,
                        description = "ワールドの種類です。"
                ),
                @TypeProperty(
                        name = "seed",
                        type = long.class,
                        description = "ワールドのシード値です。"
                ),
                @TypeProperty(
                        name = "structures",
                        type = boolean.class,
                        description = "構造物を生成するかどうかです。"
                ),
                @TypeProperty(
                        name = "env",
                        type = World.Environment.class,
                        description = "ワールドの環境です（e.g. ネザー、エンド）。"
                ),
                @TypeProperty(
                        name = "hardcore",
                        type = boolean.class,
                        description = "ハードコアモードかどうかです。"
                )
        }
)
public interface StageStructure extends Structure
{
    String KEY_ORIGINAL_WORLD_NAME = "copyOf";
    String KEY_TYPE = "type";
    String KEY_SEED = "seed";
    String KEY_GENERATE_STRUCTURES = "structures";
    String KEY_ENVIRONMENT = "env";
    String KEY_HARDCORE = "hardcore";

    /**
     * オリジナルのワールド名を取得します。
     *
     * @return ワールド名
     */
    @Nullable
    String getOriginalWorldName();

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
    boolean isGenerateStructures();

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
