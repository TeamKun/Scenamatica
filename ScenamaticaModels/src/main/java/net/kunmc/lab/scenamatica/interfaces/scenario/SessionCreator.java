package net.kunmc.lab.scenamatica.interfaces.scenario;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioNotFoundException;
import net.kunmc.lab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
     * @param plugin  シナリオを実行するプラグイン
     * @param trigger シナリオを実行するトリガー
     * @param name    シナリオの名前
     * @return セッション定義
     */
    SessionCreator add(@NotNull Plugin plugin, @NotNull TriggerType trigger, @NotNull String name);

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

        public SessionElement(Plugin plugin, String name, @NotNull TriggerType type)
        {
            this(plugin, name, type, null);
        }

        public SessionElement(ScenarioEngine engine, @NotNull TriggerType type)
        {
            this(null, null, engine, type, null);
        }

        public SessionElement(Plugin plugin, String name, @NotNull TriggerType type, @Nullable Consumer<? super ScenarioResult> callback)
        {
            this(plugin, name, null, type, callback);
        }

        public SessionElement(ScenarioEngine engine, @NotNull TriggerType type, @Nullable Consumer<? super ScenarioResult> callback)
        {
            this(null, null, engine, type, callback);
        }
    }
}
