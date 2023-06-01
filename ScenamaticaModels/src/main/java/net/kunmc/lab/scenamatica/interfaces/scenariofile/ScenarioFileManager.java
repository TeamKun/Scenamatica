package net.kunmc.lab.scenamatica.interfaces.scenariofile;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * シナリオファイルを管理するインターフェースです。
 */
public interface ScenarioFileManager
{
    /**
     * シリアライザを取得します。
     */
    @NotNull BeanSerializer getSerializer();

    /**
     * プラグインのシナリオを読み込みます。
     *
     * @param plugin プラグイン
     * @return 読み込みに成功したかどうか
     */
    boolean loadPluginScenarios(@NotNull Plugin plugin);

    /**
     * プラグインのシナリオを取得します。
     * 先に {@link #loadPluginScenarios(Plugin)} で読み込む必要があります。
     * この操作を行っていない場合は、自動で読み込むため、オーバーヘッドが発生します。
     *
     * @param plugin プラグイン
     * @return シナリオ (読み込みに失敗した場合は {@code null})
     * @see #getScenario(Plugin, String)
     */
    @Nullable Map<String, ScenarioFileBean> getPluginScenarios(@NotNull Plugin plugin);

    /**
     * シナリオを取得します。
     * 先に {@link #loadPluginScenarios(Plugin)} で読み込む必要があります。
     * この操作を行っていない場合は、自動で読み込むため、オーバーヘッドが発生します。
     *
     * @param plugin       プラグイン
     * @param scenarioName シナリオ名
     * @return シナリオ (読み込みに失敗した場合は {@code null} )
     * @see #getPluginScenarios(Plugin)
     */
    @Nullable ScenarioFileBean getScenario(@NotNull Plugin plugin, @NotNull String scenarioName);

    /**
     * プラグインのシナリオをアンロードします。
     *
     * @param plugin プラグイン
     */
    void unloadPluginScenarios(@NotNull Plugin plugin);

    /**
     * プラグインのシナリオを再読み込みします。
     * これは、内部で {@link #unloadPluginScenarios(Plugin)} と {@link #loadPluginScenarios(Plugin)} を呼び出します。
     *
     * @param plugin プラグイン
     * @return 読み込みに成功したかどうか
     * @see #unloadPluginScenarios(Plugin)
     * @see #loadPluginScenarios(Plugin)
     */
    boolean reloadPluginScenarios(@NotNull Plugin plugin);
}
