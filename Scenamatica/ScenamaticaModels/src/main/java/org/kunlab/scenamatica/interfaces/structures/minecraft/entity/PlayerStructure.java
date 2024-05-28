package org.kunlab.scenamatica.interfaces.structures.minecraft.entity;

import org.bukkit.entity.Player;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.HumanEntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;

import java.net.InetAddress;
import java.util.List;

/**
 * プレイヤを表すインターフェースです。
 */
public interface PlayerStructure extends HumanEntityStructure, Mapped<Player>
{
    String KEY_NAME = "name";
    String KEY_ONLINE = "online";
    String KEY_CONNECTION = "connection";
    String KEY_CONNECTION_IP = "ip";
    String KEY_CONNECTION_PORT = "port";
    String KEY_CONNECTION_HOSTNAME = "hostname";
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
     * プレイヤの名前を取得します。
     *
     * @return プレイヤの名前
     */
    String getName();

    /**
     * プレイヤがオンラインかどうかを取得します。
     *
     * @return プレイヤがオンラインかどうか
     */
    Boolean getOnline();

    /**
     * プレイヤのIPアドレスを取得します。
     *
     * @return プレイヤのIPアドレス
     */
    InetAddress getRemoteAddress();

    /**
     * プレイヤのポート番号を取得します。
     *
     * @return プレイヤのポート番号
     */
    Integer getPort();

    /**
     * プレイヤのホスト名を取得します。
     *
     * @return プレイヤのホスト名
     */
    String getHostName();

    /**
     * プレイヤの表示名を取得します。
     *
     * @return プレイヤの表示名
     */
    String getDisplayName();

    /**
     * プレイヤリストに表示される名前を取得します。
     *
     * @return プレイヤリストに表示される名前
     */
    String getPlayerListName();

    /**
     * プレイヤリストのヘッダーを取得します。
     *
     * @return プレイヤリストのヘッダー
     */
    String getPlayerListHeader();

    /**
     * プレイヤリストのフッターを取得します。
     *
     * @return プレイヤリストのフッター
     */
    String getPlayerListFooter();

    /**
     * コンパスのターゲットを取得します。
     *
     * @return コンパスのターゲット
     */
    LocationStructure getCompassTarget();

    /**
     * ベッドのスポーン地点を取得します。
     *
     * @return ベッドのスポーン地点
     */
    LocationStructure getBedSpawnLocation();

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
     * プレイヤが持っている権限を取得します。
     *
     * @return 権限のリスト
     */
    List<String> getActivePermissions();
}
