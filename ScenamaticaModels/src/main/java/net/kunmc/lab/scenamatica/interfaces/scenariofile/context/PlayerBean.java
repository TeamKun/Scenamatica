package net.kunmc.lab.scenamatica.interfaces.scenariofile.context;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.entities.EntityBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.entities.HumanEntityBean;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;

/**
 * プレイヤーを表すインターフェースです。
 */
public interface PlayerBean extends EntityBean, HumanEntityBean, Serializable
{
    String KEY_NAME = "name";
    String KEY_DISPLAY_NAME = "display";
    String KEY_PLAYER_LIST = "playerList";
    String KEY_PLAYER_LIST_NAME = "name";
    String KEY_PLAYER_LIST_HEADER = "header";
    String KEY_PLAYER_LIST_FOOTER = "footer";
    String KEY_COMPASS_TARGET = "compass";
    String KEY_BED_SPAWN_LOCATION = "bedLocation";
    String KEY_EXP = "exp";
    String KEY_LEVEL = "level";
    String KEY_TOTAL_EXPERIENCE = "totalExp";
    String KEY_ALLOW_FLIGHT = "flyable";
    String KEY_FLYING = "flying";
    String KEY_FLY_SPEED = "flySpeed";
    String KEY_WALK_SPEED = "walkSpeed";

    /**
     * プレイヤーの名前を取得します。
     *
     * @return プレイヤーの名前
     */
    @NotNull
    String getName();

    /**
     * プレイヤーの表示名を取得します。
     *
     * @return プレイヤーの表示名
     */
    @Nullable
    String getDisplayName();

    /**
     * プレイヤーリストに表示される名前を取得します。
     *
     * @return プレイヤーリストに表示される名前
     */
    @Nullable
    String getPlayerListName();

    /**
     * プレイヤーリストのヘッダーを取得します。
     *
     * @return プレイヤーリストのヘッダー
     */
    @Nullable
    String getPlayerListHeader();

    /**
     * プレイヤーリストのフッターを取得します。
     *
     * @return プレイヤーリストのフッター
     */
    @Nullable
    String getPlayerListFooter();

    /**
     * コンパスのターゲットを取得します。
     *
     * @return コンパスのターゲット
     */
    @Nullable
    Location getCompassTarget();

    /**
     * ベッドのスポーン地点を取得します。
     *
     * @return ベッドのスポーン地点
     */
    @Nullable
    Location getBedSpawnLocation();

    /**
     * 経験値を取得します。
     *
     * @return 経験値
     */
    @Nullable
    Integer getExp();

    /**
     * レベルを取得します。
     *
     * @return レベル
     */
    @Nullable
    Integer getLevel();

    /**
     * 総経験値を取得します。
     *
     * @return 総経験値
     */
    @Nullable
    Integer getTotalExperience();

    /**
     * 飛べるかどうかを取得します。
     *
     * @return 飛べるかどうか
     */
    boolean isAllowFlight();

    /**
     * 飛んでいるかどうかを取得します。
     *
     * @return 飛んでいるかどうか
     */
    boolean isFlying();

    /**
     * 歩行速度を取得します。
     *
     * @return 歩行速度
     */
    @Nullable
    Float getWalkSpeed();

    /**
     * 飛行速度を取得します。
     *
     * @return 飛行速度
     */
    @Nullable
    Float getFlySpeed();
}
