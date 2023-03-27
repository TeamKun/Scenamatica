package net.kunmc.lab.scenamatica.interfaces.action;

import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * 動作のインタフェースです。
 */
public interface Action<A extends ActionArgument>
{
    /**
     * 動作を実行します。
     *
     * @param argument 動作の引数
     */
    void execute(@Nullable A argument);

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
     */
    boolean isFired(@NotNull Plugin plugin, @NotNull Event event);

    /**
     * アタッチす るイベントのクラスを返します。
     */
    Class<? extends Event>[] getAttachingEvents();

    /**
     * 引数をデシリアライズします。
     *
     * @param map デシリアライズするマップ
     * @return デシリアライズされた引数
     */
    A deserializeArgument(@NotNull Map<String, Object> map);
}
