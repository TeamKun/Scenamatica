package org.kunlab.scenamatica.nms.types;

import com.mojang.authlib.GameProfile;
import org.bukkit.Location;
import org.bukkit.Server;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.nms.NMSWrapped;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;

/**
 * PlayerList の NMS 版です。
 */
public interface NMSPlayerList extends NMSWrapped
{
    @Override
    Server getBukkit();

    /**
     * プレイヤーを指定したワールドに移動します。
     *
     * @param player           移動させるプレイヤー
     * @param world            移動先のワールド
     * @param shouldCopyState  状態をコピーするかどうか
     * @param locationToSpawn  スポーンする場所
     * @param avoidSuffocation 窒息を避けるかどうか
     * @return 移動後のプレイヤー
     */
    @NotNull
    @Contract("_, _, _, _, _ -> param1")
    NMSEntityPlayer moveToWorld(@NotNull NMSEntityPlayer player, @NotNull NMSWorldServer world, boolean shouldCopyState, @Nullable Location locationToSpawn, boolean avoidSuffocation);

    /**
     * 指定したプロファイルが OP 権限を持っているかどうかを取得します。
     *
     * @param profile プロファイル
     * @return OP 権限を持っているかどうか
     */
    boolean isOp(@NotNull GameProfile profile);

    /**
     * 指定したプロファイルに OP 権限を付与します。
     *
     * @param profile プロファイル
     */
    void addOp(@NotNull GameProfile profile);

    /**
     * 指定したプロファイルから OP 権限を剥奪します。
     *
     * @param profile プロファイル
     */
    void removeOp(@NotNull GameProfile profile);
}
