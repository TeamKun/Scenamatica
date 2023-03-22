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
     * 役者を生成します。
     *
     * @param stage ステージ
     * @param bean  プレイヤー情報
     * @return 生成した役者
     */
    Player createActor(World stage, PlayerBean bean);

    /**
     * 役者を破棄します。
     *
     * @param player 役者
     */
    void destroyActor(Player player);

    /**
     * 役者が廃棄されたときに呼び出されるメソッドです。
     *
     * @param player 役者
     */
    void onDestroyActor(Player player);

    /**
     * 生成された役者を全て解除し、インスタンスを破棄します。
     */
    void shutdown();
}
