package net.kunmc.lab.scenamatica.interfaces.scenario;

import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioAlreadyRunningException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioNotFoundException;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioNotRunningException;
import net.kunmc.lab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * シナリオを管理するインターフェースです。
 */
public interface ScenarioManager
{
    /**
     * このシナリオマネージャーを初期化します。
     */

    void init();
    /**
     * シナリオが実行中かどうかを返します。
     *
     * @return 実行中なら {@code true}
     */
    boolean isRunning();

    /**
     * シナリオを割り込み手動実行します。
     * 手動実行するシナリオは、トリガに {@link net.kunmc.lab.scenamatica.enums.TriggerType#MANUAL_DISPATCH} を指定している必要があります。
     *
     * @param plugin        シナリオを実行するプラグイン
     * @param scenarioName  シナリオ名
     * @param cancelRunning 実行中の他のシナリオをキャンセルして実行するかどうか
     * @return シナリオの実行結果
     * @throws IllegalStateException           シナリオ実行が有効でない場合
     * @throws ScenarioAlreadyRunningException 他のシナリオが実行中で、{@code cancelRunning} が {@code false} の場合
     * @throws IllegalArgumentException        シナリオが手動実行できない場合
     */
    @NotNull
    TestResult startScenarioInterrupt(@NotNull Plugin plugin,
                                      @NotNull String scenarioName,
                                      boolean cancelRunning) throws ScenarioException;

    /**
     * シナリオを割り込み実行します。
     *
     * @param plugin        シナリオを実行するプラグイン
     * @param scenarioName  シナリオ名
     * @param triggerType   トリガの種類
     * @param cancelRunning 実行中の他のシナリオをキャンセルして実行するかどうか
     * @return シナリオの実行結果
     * @throws IllegalStateException           シナリオ実行が有効でない場合
     * @throws ScenarioAlreadyRunningException 他のシナリオが実行中で、{@code cancelRunning} が {@code false} の場合
     * @throws ScenarioNotFoundException       シナリオが存在しない場合
     * @throws TriggerNotFoundException        シナリオが指定されたトリガで実行できない場合
     */
    @NotNull
    TestResult startScenarioInterrupt(@NotNull Plugin plugin,
                                      @NotNull String scenarioName,
                                      @NotNull TriggerType triggerType,
                                      boolean cancelRunning) throws ScenarioException;

    /**
     * シナリオを割り込み手動実行します。
     * 手動実行するシナリオは、トリガに {@link net.kunmc.lab.scenamatica.enums.TriggerType#MANUAL_DISPATCH} を指定している必要があります。
     * 実行中の他のシナリオはキャンセルされません。
     *
     * @param plugin       シナリオを実行するプラグイン
     * @param scenarioName シナリオ名
     * @return シナリオの実行結果
     * @throws IllegalStateException           シナリオ実行が有効でない場合
     * @throws ScenarioAlreadyRunningException 他のシナリオが実行中の場合
     * @throws ScenarioNotFoundException       シナリオが存在しない場合
     * @throws TriggerNotFoundException        シナリオが手動実行できない場合
     * @see #startScenarioInterrupt(Plugin, String, TriggerType, boolean)
     */
    @NotNull
    default TestResult startScenarioInterrupt(@NotNull Plugin plugin,
                                              @NotNull String scenarioName) throws ScenarioException
    {
        return startScenarioInterrupt(plugin, scenarioName, false);
    }

    /**
     * シナリオを割り込み実行します。
     * 実行中の他のシナリオはキャンセルされません。
     *
     * @param plugin       シナリオを実行するプラグイン
     * @param scenarioName シナリオ名
     * @param triggerType  トリガの種類
     * @return シナリオの実行結果
     * @throws IllegalStateException           シナリオ実行が有効でない場合
     * @throws ScenarioAlreadyRunningException 他のシナリオが実行中の場合
     * @throws ScenarioNotFoundException       シナリオが存在しない場合
     * @throws TriggerNotFoundException        シナリオが指定されたトリガで実行できない場合
     * @see #startScenarioInterrupt(Plugin, String, boolean)
     * @see #startScenarioInterrupt(Plugin, String, TriggerType, boolean)
     */
    @NotNull
    default TestResult startScenarioInterrupt(@NotNull Plugin plugin,
                                              @NotNull String scenarioName,
                                              @NotNull TriggerType triggerType) throws ScenarioException
    {
        return startScenarioInterrupt(plugin, scenarioName, triggerType, false);
    }

    /**
     * シナリオの実行をキューに追加します。
     *
     * @param plugin       シナリオを実行するプラグイン
     * @param scenarioName シナリオ名
     * @param triggerType  トリガの種類
     * @throws ScenarioNotFoundException シナリオが存在しない場合
     * @throws TriggerNotFoundException  指定されたトリガが存在しない場合
     */
    void queueScenario(@NotNull Plugin plugin,
                       @NotNull String scenarioName,
                       @NotNull TriggerType triggerType) throws ScenarioNotFoundException, TriggerNotFoundException;

    /**
     * プラグインのシナリオをキューから削除します。
     *
     * @param plugin 削除するプラグイン
     */
    void dequeueScenarios(@NotNull Plugin plugin);

    /**
     * シナリオをキューから削除します。
     *
     * @param plugin       削除するプラグイン
     * @param scenarioName 削除するシナリオ名
     */
    void dequeueScenario(@NotNull Plugin plugin, @NotNull String scenarioName);

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
