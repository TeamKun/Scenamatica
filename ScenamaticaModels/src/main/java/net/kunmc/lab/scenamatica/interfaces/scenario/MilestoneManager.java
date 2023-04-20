package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.enums.MilestoneScope;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * マイルストーンを管理するインターフェースです。
 */
public interface MilestoneManager
{
    /**
     * マイルストーンを達成します。
     *
     * @param engine シナリオエンジン
     * @param name   マイルストーン名
     * @return 達成に成功した場合は {@code true}
     */
    boolean reachMilestone(@NotNull ScenarioEngine engine, @NotNull String name);

    /**
     * マイルストーンを達成します。
     *
     * @param engine シナリオエンジン
     * @param name   マイルストーン名
     * @param scope  スコープ
     * @return 達成に成功した場合は {@code true}
     */
    boolean reachMilestone(@NotNull ScenarioEngine engine, @NotNull String name, @NotNull MilestoneScope scope);

    /**
     * マイルストーンを達成します。
     *
     * @param entry マイルストーンエントリ
     * @return 達成に成功した場合は {@code true}
     */
    boolean reachMilestone(@NotNull MilestoneEntry entry);

    /**
     * マイルストーンが達成されているかどうかを返します。
     *
     * @param engine シナリオエンジン
     * @param name   マイルストーン名
     * @return 達成されている場合は {@code true}
     */
    boolean isReached(@NotNull ScenarioEngine engine, @NotNull String name);

    /**
     * マイルストーンを取り消します。
     *
     * @param entry マイルストーンエントリ
     */
    void revokeMilestone(@NotNull MilestoneEntry entry);

    /**
     * マイルストーンを取り消します。
     *
     * @param engine シナリオエンジン
     * @param name   マイルストーン名
     */
    void revokeMilestone(@NotNull ScenarioEngine engine, @NotNull String name);

    /**
     * マイルストーンを取り消します。
     *
     * @param engine 関連付けられたシナリオエンジン
     */
    void revokeAllMilestones(@NotNull ScenarioEngine engine);

    /**
     * マイルストーンを取り消します。
     *
     * @param engine 関連付けられたシナリオエンジン
     * @param scope  マイルストーンのスコープ
     */
    void revokeAllMilestones(@NotNull ScenarioEngine engine, @NotNull MilestoneScope scope);

    /**
     * マイルストーンを取り消します。
     *
     * @param plugin 関連付けられたプラグイン
     */
    void revokeAllMilestones(@NotNull Plugin plugin);

    /**
     * マイルストーンを取得します。
     *
     * @param engine シナリオエンジン
     * @param name   マイルストーン名
     * @return マイルストーンエントリ
     */
    @Nullable MilestoneEntry getMilestone(@NotNull ScenarioEngine engine, @NotNull String name);
}
