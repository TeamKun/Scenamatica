package org.kunlab.scenamatica.interfaces.structures.context;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;

import java.util.List;

/**
 * シナリオの実行に必要な情報を表すインターフェースです。
 */
public interface ContextStructure extends Structure
{
    /**
     * 仮想プレイヤーを定義します。
     *
     * @return 仮想プレイヤー
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
