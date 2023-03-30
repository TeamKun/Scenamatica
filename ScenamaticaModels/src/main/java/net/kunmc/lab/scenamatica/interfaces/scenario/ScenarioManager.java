package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.exceptions.context.ContextPreparationException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioNotRunningException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * シナリオを管理するインターフェースです。
 */
public interface ScenarioManager
{
    /**
     * シナリオが実行中かどうかを返します。
     *
     * @return 実行中なら {@code true}
     */
    boolean isRunning();

    /**
     * シナリオを手動実行します。
     * 手動実行するシナリオは、トリガに {@link net.kunmc.lab.scenamatica.enums.TriggerType#MANUAL_DISPATCH} を指定している必要があります。
     *
     * @param plugin       シナリオを実行するプラグイン
     * @param scenarioName シナリオ名
     * @return シナリオの実行結果
     * @throws IllegalArgumentException シナリオが存在しない場合
     * @throws IllegalArgumentException シナリオが手動実行できない場合
     */
    @NotNull
    TestResult startScenario(@NotNull Plugin plugin, @NotNull String scenarioName) throws ContextPreparationException, ScenarioException;

    /**
     * シナリオを実行します。
     *
     * @param plugin       シナリオを実行するプラグイン
     * @param scenarioName シナリオ名
     * @param triggerType  トリガの種類
     * @return シナリオの実行結果
     * @throws IllegalArgumentException シナリオが存在しない場合
     * @throws IllegalArgumentException シナリオが指定されたトリガで実行できない場合
     */
    @NotNull
    TestResult startScenario(@NotNull Plugin plugin, @NotNull String scenarioName, @NotNull TriggerType triggerType) throws ContextPreparationException, ScenarioException;

    /**
     * シナリオの実行をキャンセルします。
     * キャンセルしたシナリオは, {@link net.kunmc.lab.scenamatica.enums.TestResultCause#CANCELLED} で終了します。
     */
    void cancel() throws ScenarioNotRunningException;

    /**
     * プラグインのシナリオをアンロードします。
     *
     * @param plugin アンロードするプラグイン
     */
    void unloadPluginScenarios(@NotNull Plugin plugin);

    /**
     * プラグインのシナリオをロードします。
     * ロードされたシナリオは, 実行時コンパイルされます。
     *
     * @param plugin ロードするプラグイン
     */
    void loadPluginScenarios(@NotNull Plugin plugin);

    /**
     * プラグインのシナリオをリロードします。
     * このメソッドは、 内部で {@link #unloadPluginScenarios(Plugin)} と {@link #loadPluginScenarios(Plugin)} を呼び出します。
     *
     * @param plugin リロードするプラグイン
     * @see #unloadPluginScenarios(Plugin)
     * @see #loadPluginScenarios(Plugin)
     */
    void reloadPluginScenarios(@NotNull Plugin plugin);

    /**
     * 現在実行中のシナリオを返します。
     *
     * @return シナリオ
     */
    @Nullable
    ScenarioEngine getCurrentScenario();

    /**
     * 実行中のシナリオを停止し, このインスタンスを破棄します。
     */
    void shutdown();

    /**
     * シナリオの実行が有効かどうかを返します。
     *
     * @return 有効なら {@code true}
     */
    boolean isEnabled();

    /**
     * シナリオの実行を有効にします。
     *
     * @param enabled 有効にするなら {@code true}
     */
    void setEnabled(boolean enabled);
}
