package net.kunmc.lab.scenamatica.interfaces.scenariofile.context;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * シナリオの実行に必要な情報を表すインターフェースです。
 */
public interface ContextBean
{
    /**
     * 仮想プレイヤーを定義します。
     *
     * @return 仮想プレイヤー
     */
    @Nullable
    List<PlayerBean> getActors();

    /**
     * ワールドを定義します。
     *
     * @return ワールド
     */
    @Nullable
    StageBean getWorld();
}
