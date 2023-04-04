package net.kunmc.lab.scenamatica.settings;

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
}
