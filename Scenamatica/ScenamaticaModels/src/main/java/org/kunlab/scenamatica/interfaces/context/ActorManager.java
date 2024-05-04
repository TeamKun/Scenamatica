package org.kunlab.scenamatica.interfaces.context;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.exceptions.context.ContextPreparationException;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.PlayerStructure;

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
     * @param defaultWOrld
     * @param structure    プレイヤー情報
     * @return 生成した役者
     */
    Actor createActor(@NotNull World defaultWOrld, @NotNull PlayerStructure structure) throws ContextPreparationException;

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

    /**
     * 名前からアクターを取得します。
     *
     * @param name プレイヤーの名前
     */
    @Nullable
    Actor getByName(@NotNull String name);
}
