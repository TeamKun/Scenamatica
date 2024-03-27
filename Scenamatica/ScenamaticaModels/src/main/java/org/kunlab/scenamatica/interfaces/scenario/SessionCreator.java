package org.kunlab.scenamatica.interfaces.scenario;

import lombok.AllArgsConstructor;
import lombok.Value;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioNotFoundException;
import org.kunlab.scenamatica.exceptions.scenario.TriggerNotFoundException;

import java.util.List;
import java.util.function.Consumer;

/**
 * シナリオを実行するためのセッションを作成するクラスです。
 */
public interface SessionCreator
{
    /**
     * セッション定義にシナリオを追加します。
     *
     * @param plugin     シナリオを実行するプラグイン
     * @param trigger    シナリオを実行するトリガー
     * @param name       シナリオの名前
     * @param maxAttempt 最大試行回数
     * @return セッション定義
     */
    SessionCreator add(@NotNull Plugin plugin, @NotNull TriggerType trigger, @NotNull String name, int maxAttempt);

    /**
     * セッション定義にシナリオを追加します。
     *
     * @param plugin  シナリオを実行するプラグイン
     * @param trigger シナリオを実行するトリガー
     * @param name    シナリオの名前
     * @return セッション定義
     */
    SessionCreator add(@NotNull Plugin plugin, @NotNull TriggerType trigger, @NotNull String name);

    /**
     * セッション定義にシナリオを追加します。
     *
     * @param engine     シナリオを実行するエンジン
     * @param trigger    シナリオを実行するトリガー
     * @param maxAttempt 最大試行回数
     * @return セッション定義
     */
    SessionCreator add(@NotNull ScenarioEngine engine, @NotNull TriggerType trigger, int maxAttempt);

    /**
     * セッション定義にシナリオを追加します。
     *
     * @param engine  シナリオを実行するエンジン
     * @param trigger シナリオを実行するトリガー
     * @return セッション定義
     */
    SessionCreator add(@NotNull ScenarioEngine engine, @NotNull TriggerType trigger);

    /**
     * セッション定義にシナリオを追加します。
     *
     * @return セッション定義
     */
    List<SessionElement> getSessions();

    /**
     * セッション定義に追加されたシナリオを実行します。
     *
     * @throws TriggerNotFoundException  トリガーが見つからない場合
     * @throws ScenarioNotFoundException シナリオが見つからない場合
     */
    void queueAll() throws TriggerNotFoundException, ScenarioNotFoundException;

    /**
     * セッション定義に追加されたシナリオが空かどうかを返します。
     *
     * @return セッション定義に追加されたシナリオが空かどうか
     */
    boolean isEmpty();

    @Value
    @AllArgsConstructor
    class SessionElement
    {
        Plugin plugin;
        String name;
        // or
        ScenarioEngine engine;

        @NotNull
        TriggerType type;
        @Nullable
        Consumer<? super ScenarioResult> callback;

        int maxAttempt;

        public SessionElement(Plugin plugin, String name, @NotNull TriggerType type, int maxAttempt)
        {
            this(plugin, name, null, type, null, maxAttempt);
        }

        public SessionElement(ScenarioEngine engine, @NotNull TriggerType type, int maxAttempt)
        {
            this(null, null, engine, type, null, maxAttempt);
        }
    }
}
