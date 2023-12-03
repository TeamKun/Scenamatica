package org.kunlab.scenamatica.interfaces.scenariofile.context;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.GenericEntityStructure;

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
    List<GenericEntityStructure> getEntities();

    /**
     * ワールドを定義します。
     *
     * @return ワールド
     */
    StageStructure getWorld();
}
