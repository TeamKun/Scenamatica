package net.kunmc.lab.scenamatica.interfaces.context;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * シナリオを実行する役者を管理するインターフェースです。
 */
public interface ActorManager
{
    /**
     * プレイヤーをモックします。
     *
     * @param stage ステージ
     * @param bean  プレイヤー情報
     * @return モックプレイヤー
     */
    Player mock(World stage, PlayerBean bean);

    /**
     * プレイヤーのモックを解除します。
     *
     * @param player プレイヤー
     */
    void unmock(Player player);

    /**
     * モックプレイヤーを全て解除し、インスタンスを破棄します。
     */
    void shutdown();
}
