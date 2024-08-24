package org.kunlab.scenamatica.interfaces.structures.minecraft.entity;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.HumanEntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;

/**
 * プレイヤを表すインターフェースです。
 */
@TypeDoc(
        name = "Player",
        description = "プレイヤの情報を格納します。",
        mappingOf = Player.class,
        properties = {
                @TypeProperty(
                        name = PlayerStructure.KEY_NAME,
                        description = "プレイヤの名前です。",
                        type = String.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_ONLINE,
                        description = "プレイヤがオンラインかどうかです。\n" +
                                "アクタの定義でかつこの値が `false` の場合は, アクタは準備されますがログイン処理はスキップされます。\n" +
                                "\n" +
                                "`PlayerJoinEvent` のテスト時などに, `false` にすることで余計な発火を防止してテストできます。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_CONNECTION,
                        description = "プレイヤの接続情報です。",
                        type = Map.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_DISPLAY_NAME,
                        description = "プレイヤの表示名です。",
                        type = String.class,
                        admonitions = {
                                @Admonition(
                                        type = AdmonitionType.WARNING,
                                        content = "この項目は Bukkit との互換性を保つために存在しています。\n" +
                                                "必ずしも表示名として使用されるわけではありません。"
                                )
                        }
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_PLAYER_LIST,
                        description = "プレイヤのプレイヤリストに表示される情報です。",
                        type = Map.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_COMPASS_TARGET,
                        description = "プレイヤのコンパスが指す位置です。",
                        type = Location.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_BED_SPAWN_LOCATION,
                        description = "プレイヤのベッドのスポーン地点です。",
                        type = Location.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_EXP,
                        description = "プレイヤの経験値です。",
                        type = int.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_LEVEL,
                        description = "プレイヤのレベルです。",
                        type = int.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_TOTAL_EXPERIENCE,
                        description = "プレイヤの総経験値です。",
                        type = int.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_ALLOW_FLIGHT,
                        description = "プレイヤが飛べるかどうかです。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_FLYING,
                        description = "プレイヤが飛んでいるかどうかです。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_FLY_SPEED,
                        description = "プレイヤの飛行速度です。",
                        type = float.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_WALK_SPEED,
                        description = "プレイヤの歩行速度です。",
                        type = float.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_OP_LEVEL,
                        description = "プレイヤの OP レベルです。\n" +
                                "真偽値も指定でき, その場合は Scenamatica の設定ファイルで設定したデフォルトの OP 権限レベルが適用されます。\n" +
                                "\n" +
                                "レベルの整数値と権限の強さは以下の通りです：\n" +
                                "\n" +
                                "+ `0` - OP 権限なし\n" +
                                "+ `1` - OP 権限あり。スポーン範囲内制限等をバイパスできる。\n" +
                                "+ `2` - 上記に加え, ちょと多くのコマンドを使える。コマンドブロックを使える。\n" +
                                "+ `3` - 上記に加え, もっといっぱいコマンドを使える。\n" +
                                "+ `4` - 上記に加え, ほぼ全てのコマンドを使える。",
                        type = int.class
                ),
                @TypeProperty(
                        name = PlayerStructure.KEY_ACTIVE_PERMISSIONS,
                        description = "プレイヤが持っている権限です。\n" +
                                "Scenamatica の設定ファイルで設定したデフォルトの権限があらかじめ適用されます。",
                        type = String[].class
                )
        }
)
public interface PlayerStructure extends HumanEntityStructure
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
