package org.kunlab.scenamatica.interfaces.scenariofile.specifiers;

import org.bukkit.entity.Player;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;

/**
 * プレイヤの指定子を提供します。
 */
public interface PlayerSpecifier extends EntitySpecifier<Player>
{
    /**
     * ターゲットの構造を取得します。
     *
     * @return 構造
     */
    @Override
    PlayerStructure getTargetStructure();

    /**
     * マッチしたプレイヤを取得します。
     *
     * @param player プレイヤ
     * @return マッチしたかどうか
     */
    boolean checkMatchedPlayer(Player player);
}
