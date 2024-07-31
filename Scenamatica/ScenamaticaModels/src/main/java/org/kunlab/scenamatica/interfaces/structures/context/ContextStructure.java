package org.kunlab.scenamatica.interfaces.structures.context;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;

import java.util.List;

/**
 * シナリオの実行に必要な情報を表すインターフェースです。
 */
@TypeDoc(
        name = "コンテキスト",
        description = "シナリオの実行に必要な前提情報を表します。",
        properties = {
                @TypeProperty(
                        name = ContextStructure.KEY_ACTORS,
                        type = PlayerStructure[].class,
                        description = "仮想プレイヤを定義します。"
                ),
                @TypeProperty(
                        name = ContextStructure.KEY_ENTITIES,
                        type = EntityStructure[].class,
                        description = "エンティティを定義します。"
                ),
                @TypeProperty(
                        name = ContextStructure.KEY_STAGE,
                        type = StageStructure.class,
                        description = "ワールドを定義します。"
                )
        }
)
public interface ContextStructure extends Structure
{
    public static final String KEY_ACTORS = "actors";
    public static final String KEY_ENTITIES = "entities";
    public static final String KEY_STAGE = "stage";


    /**
     * 仮想プレイヤを定義します。
     *
     * @return 仮想プレイヤ
     */
    @NotNull
    List<PlayerStructure> getActors();

    /**
     * エンティティを定義します。
     *
     * @return エンティティ
     */
    @NotNull
    List<EntityStructure> getEntities();

    /**
     * ワールドを定義します。
     *
     * @return ワールド
     */
    StageStructure getWorld();
}
