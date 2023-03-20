package net.kunmc.lab.scenamatica.interfaces.scenariofile.context;

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
    List<PlayerBean> getPseudoPlayers();

    /**
     * ワールドを定義します。
     *
     * @return ワールド
     */
    WorldBean getWorld();
}
