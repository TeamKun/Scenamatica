package net.kunmc.lab.scenamatica.interfaces.context;

import net.kunmc.lab.scenamatica.exceptions.context.actor.ActorAlreadyExistsException;
import net.kunmc.lab.scenamatica.exceptions.context.stage.StageNotCreatedException;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * シナリオを実行する役者を管理するインターフェースです。
 */
public interface ActorManager
{
    /**
     * 役者を生成します。
     *
     * @param bean プレイヤー情報
     * @return 生成した役者
     */
    Player createActor(PlayerBean bean) throws ActorAlreadyExistsException, StageNotCreatedException;

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
     * 登録されている役者をすべて取得します。
     *
     * @return 役者
     */
    List<Player> getActors();

    /**
     * 生成された役者を全て解除し、インスタンスを破棄します。
     */
    void shutdown();

    /**
     * 指定したプレイヤーが役者かどうかを返します。
     *
     * @param player プレイヤー
     * @return 役者かどうか
     */
    boolean isActor(Player player);
}
