package org.kunlab.scenamatica.interfaces.scenariofile.context;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.GenericHumanEntityStructure;

import java.util.List;

/**
 * プレイヤーを表すインターフェースです。
 */
public interface PlayerStructure extends GenericHumanEntityStructure, Mapped<Player>
{
    String KEY_NAME = "name";
    String KEY_ONLINE = "online";
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

    String KEY_OP_LEVEL = "op";
    String KEY_ACTIVE_PERMISSIONS = "permissions";

    /**
     * プレイヤーの名前を取得します。
     *
     * @return プレイヤーの名前
     */
    String getName();

    /**
     * プレイヤーがオンラインかどうかを取得します。
     *
     * @return プレイヤーがオンラインかどうか
     */
    Boolean getOnline();

    /**
     * プレイヤーの表示名を取得します。
     *
     * @return プレイヤーの表示名
     */
    String getDisplayName();

    /**
     * プレイヤーリストに表示される名前を取得します。
     *
     * @return プレイヤーリストに表示される名前
     */
    String getPlayerListName();

    /**
     * プレイヤーリストのヘッダーを取得します。
     *
     * @return プレイヤーリストのヘッダー
     */
    String getPlayerListHeader();

    /**
     * プレイヤーリストのフッターを取得します。
     *
     * @return プレイヤーリストのフッター
     */
    String getPlayerListFooter();

    /**
     * コンパスのターゲットを取得します。
     *
     * @return コンパスのターゲット
     */
    Location getCompassTarget();

    /**
     * ベッドのスポーン地点を取得します。
     *
     * @return ベッドのスポーン地点
     */
    Location getBedSpawnLocation();

    /**
     * 経験値を取得します。
     *
     * @return 経験値
     */
    Integer getExp();

    /**
     * レベルを取得します。
     *
     * @return レベル
     */
    Integer getLevel();

    /**
     * 総経験値を取得します。
     *
     * @return 総経験値
     */
    Integer getTotalExperience();

    /**
     * 飛べるかどうかを取得します。
     *
     * @return 飛べるかどうか
     */
    Boolean getAllowFlight();

    /**
     * 飛んでいるかどうかを取得します。
     *
     * @return 飛んでいるかどうか
     */
    Boolean getFlying();

    /**
     * 歩行速度を取得します。
     *
     * @return 歩行速度
     */
    Float getWalkSpeed();

    /**
     * 飛行速度を取得します。
     *
     * @return 飛行速度
     */
    Float getFlySpeed();

    /**
     * プレイヤの OP レベルを取得します。
     *
     * @return OP レベル
     */
    Integer getOpLevel();

    /**
     * プレイヤーが持っている権限を取得します。
     *
     * @return 権限のリスト
     */
    List<String> getActivePermissions();
}
