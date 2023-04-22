package net.kunmc.lab.scenamatica.interfaces.action;

import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

/**
 * 動作のインタフェースです。
 */
public interface Action<A extends ActionArgument>
{
    /**
     * 動作のシリアライズ名を返します。
     *
     * @return 動作のシリアライズ名
     */
    String getName();

    /**
     * 動作を実行します。
     *
     * @param engine   シナリオエンジン
     * @param argument 動作の引数
     */
    void execute(@NotNull ScenarioEngine engine, @Nullable A argument);

    /**
     * 動作が実行されたかチェックするバスに登録されたときに呼び出されます。
     *
     * @param argument 動作の引数
     * @param plugin   プラグイン
     * @param event    これに関連するイベント
     */
    void onStartWatching(@Nullable A argument, @NotNull Plugin plugin, @Nullable Event event);

    /**
     * 動作が実行されたかどうかを返します。
     *
     * @param argument 動作の引数
     * @param engine   シナリオエンジン
     * @param event    これに関連するイベント
     * @return 動作が実行されたかどうか
     */
    boolean isFired(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event);

    /**
     * アタッチす るイベントのクラスを返します。
     */
    List<Class<? extends Event>> getAttachingEvents();

    /**
     * 引数をデシリアライズします。
     *
     * @param map デシリアライズするマップ
     * @return デシリアライズされた引数
     */
    A deserializeArgument(@NotNull Map<String, Object> map);

    /**
     * 引数が正しいかチェックします。
     *
     * @param engine   シナリオエンジン
     * @param type     シナリオの種類
     * @param argument 引数
     */
    void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable A argument);
}
