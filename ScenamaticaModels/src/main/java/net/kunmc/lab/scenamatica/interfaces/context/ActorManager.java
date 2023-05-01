package net.kunmc.lab.scenamatica.interfaces.context;

import net.kunmc.lab.scenamatica.exceptions.context.ContextPreparationException;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * シナリオを実行する役者を管理するインターフェースです。
 */
public interface ActorManager
{
    /**
     * 役者を生成します。
     * このメソッドは非同期で実行する必要があります。
     *
     * @param bean プレイヤー情報
     * @return 生成した役者
     */
    Actor createActor(PlayerBean bean) throws ContextPreparationException;

    /**
     * 役者を破棄します。
     *
     * @param player 役者
     */
    void destroyActor(Actor player);

    /**
     * 役者が廃棄されたときに呼び出されるメソッドです。
     *
     * @param player 役者
     */
    void onDestroyActor(Actor player);

    /**
     * 登録されている役者をすべて取得します。
     *
     * @return 役者
     */
    List<Actor> getActors();

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
    boolean isActor(@NotNull Player player);

    /**
     * {@link java.util.UUID} から アクターを取得します。
     *
     * @param uuid プレイヤーのUUID
     */
    @Nullable
    Actor getByUUID(@NotNull UUID uuid);
}
