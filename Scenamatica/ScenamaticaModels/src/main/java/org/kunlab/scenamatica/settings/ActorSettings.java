package org.kunlab.scenamatica.settings;

import java.util.List;

/**
 * アクターの設定を表すインターフェースです。
 */
public interface ActorSettings
{
    /**
     * アクターの最大数を取得します。
     *
     * @return アクターの最大数
     */
    long getMaxActors();

    /**
     * デフォルトのOPレベルを取得します。
     *
     * @return デフォルトのOPレベル
     */
    int getDefaultOPLevel();

    /**
     * デフォルトの権限を取得します。
     *
     * @return デフォルトの権限
     */
    List<String> getDefaultPermissions();

    /**
     * デフォルトのスコアボードタグを取得します。
     *
     * @return デフォルトのスコアボードタグ
     */
    List<String> getDefaultScoreboardTags();

    /**
     * デフォルトの IP アドレスを取得します。
     *
     * @return デフォルトの IP アドレス
     */
    String getDefaultSocketAddress();

    /**
     * デフォルトのポート番号を取得します。
     *
     * @return デフォルトのポート番号
     */
    int getDefaultSocketPort();
}
