package org.kunlab.scenamatica.interfaces.scenariofile.context;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.Bean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityBean;

import java.util.List;

/**
 * シナリオの実行に必要な情報を表すインターフェースです。
 */
public interface ContextBean extends Bean
{
    /**
     * 仮想プレイヤーを定義します。
     *
     * @return 仮想プレイヤー
     */
    @NotNull
    List<PlayerBean> getActors();

    /**
     * エンティティを定義します。
     *
     * @return エンティティ
     */
    @NotNull
    List<EntityBean> getEntities();

    /**
     * ワールドを定義します。
     *
     * @return ワールド
     */
    StageBean getWorld();
}
