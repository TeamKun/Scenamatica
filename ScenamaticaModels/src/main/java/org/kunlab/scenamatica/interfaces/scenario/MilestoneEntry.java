package org.kunlab.scenamatica.interfaces.scenario;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.MilestoneScope;

/**
 * マイルストーンのエントリです。
 */
public interface MilestoneEntry
{
    /**
     * マイルストーンの名前を取得します。
     *
     * @return マイルストーンの名前
     */
    @NotNull
    String getName();

    /**
     * マイルストーンに関連付けられたプラグインを取得します。
     *
     * @return マイルストーンに関連付けられたプラグイン
     */
    @NotNull
    Plugin getPlugin();

    /**
     * マイルストーンのスコープを取得します。
     *
     * @return マイルストーンのスコープ
     */
    @NotNull
    MilestoneScope getScope();

    /**
     * マイルストーンのスコープを設定します。
     *
     * @param scope マイルストーンのスコープ
     */
    void setScope(@NotNull MilestoneScope scope);

    /**
     * マイルストーンに関連付けられたシナリオエンジンを取得します。
     *
     * @return マイルストーンに関連付けられたシナリオエンジン
     */
    @NotNull
    ScenarioEngine getEngine();

    /**
     * マイルストーンが到達したかどうかを取得します。
     *
     * @return 到達した場合は {@code true}
     */
    boolean isReached();

    /**
     * マイルストーンが到達したかどうかを設定します。
     *
     * @param reached 到達した場合は {@code true}
     */
    void setReached(boolean reached);
}
