package org.kunlab.scenamatica.interfaces.action.types;

import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.List;

/**
 * 監視可能な動作を表すインターフェースです。
 *
 * @param <A> 動作の引数の型
 */
public interface Watchable<A extends ActionArgument>
{
    /**
     * 動作が実行されたかチェックするバスに登録されたときに呼び出されます。
     *
     * @param argument 動作の引数
     * @param plugin   プラグイン
     * @param event    これに関連するイベント
     */
    default void onStartWatching(A argument, @NotNull Plugin plugin, @Nullable Event event)
    {
    }

    /**
     * 動作が実行されたかどうかを返します。
     *
     * @param argument 動作の引数
     * @param engine   シナリオエンジン
     * @param event    これに関連するイベント
     * @return 動作が実行されたかどうか
     */
    boolean isFired(A argument, @NotNull ScenarioEngine engine, @NotNull Event event);

    /**
     * アタッチす るイベントのクラスを返します。
     */
    List<Class<? extends Event>> getAttachingEvents();
}
