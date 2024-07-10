package org.kunlab.scenamatica.interfaces.action.types;

import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.action.ActionContext;

import java.util.List;

/**
 * 監視可能な動作を表すインターフェースです。
 */
public interface Expectable
{
    /**
     * 動作が実行されたかチェックするバスに登録されたときに呼び出されます。
     *
     * @param ctxt  動作の実行コンテキスト
     * @param event これに関連するイベント
     */
    default void onStartWatching(@NotNull ActionContext ctxt, @Nullable Event event)
    {
    }

    /**
     * 動作が実行されたかどうかを返します。
     *
     * @param ctxt  動作の実行コンテキスト
     * @param event これに関連するイベント
     * @return 動作が実行された場合は true
     */
    boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event);

    /**
     * アタッチす るイベントのクラスを返します。
     */
    List<Class<? extends Event>> getAttachingEvents();
}
