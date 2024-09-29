package org.kunlab.scenamatica.interfaces.action;

import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

/**
 * このインターフェースは、アプリケーション内のアクションを管理するためのメソッドを提供します。
 * アクションのロード、アンロード、リロード、および名前によるアクションの取得が可能です。
 */
public interface ActionLoader
{
    /**
     * アプリケーションを初期化します。
     *
     * @param scenamatica アプリケーション
     */
    void init(@NotNull Plugin scenamatica);

    /**
     * アプリケーションにロードされたすべてのアクションをリロードします。
     * このメソッドは、アクションに更新や変更があってアプリケーションに反映させる必要があるときに便利です。
     */
    void reloadActions();

    /**
     * 指定したプラグインからアクションをアプリケーションにロードします。
     *
     * @param plugin アクションをロードするプラグイン。
     */
    void loadActions(Plugin plugin);

    /**
     * アプリケーションから指定したプラグインのアクションをアンロードします。
     *
     * @param plugin アクションをアンロードするプラグイン。
     */
    void unloadActions(Plugin plugin);

    /**
     * 名前によりアクションを取得します。
     *
     * @param name 取得するアクションの名前。
     * @return 指定した名前に一致するアクション。
     */
    <T extends Action> LoadedAction<T> getActionByName(@NotNull String name);

    /**
     * クラスによりアクションを取得します。
     *
     * @param clazz 取得するアクションのクラス。
     * @return 指定したクラスに一致するアクション。
     */
    <T extends Action> LoadedAction<T> getActionByClass(@NotNull Class<T> clazz);

    /**
     * 名前により最も類似したアクションを取得します。
     * このメソッドは、指定した名前に一致するアクションが見つからない場合に、最も類似したアクションを取得します。
     *
     * @param name 取得するアクションの名前。
     * @return 指定した名前に一致するアクション、または最も類似したアクション。
     */
    LoadedAction<?> getMostSimilarActionByName(@NotNull String name);

    /**
     * アプリケーションをシャットダウンします。
     */
    void shutdown();
}
