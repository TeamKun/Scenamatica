package net.kunmc.lab.scenamatica.scenario.beans.context;

import org.jetbrains.annotations.Nullable;


/**
 * シナリオの実行に必要な情報を表すクラスです。
 */
public class ContextBean
{
    /**
     * 仮想プレイヤーを定義します。
     */
    @Nullable
    PlayerBean[] pseudoPlayers;

    /**
     * ワールドを定義します。
     */
    @Nullable
    WorldBean world;
}
