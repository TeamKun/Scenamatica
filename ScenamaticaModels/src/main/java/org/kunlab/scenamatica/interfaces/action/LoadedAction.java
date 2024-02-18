package org.kunlab.scenamatica.interfaces.action;

import org.bukkit.plugin.Plugin;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

/**
 * このインターフェースは、アクションのロードされたインスタンスを管理します。
 * 実行可能、監視可能、要求可能な形式でアクションを取得したり、アクションの所有者や名前を取得したりします。
 */
public interface LoadedAction<T extends Action>
{
    /**
     * アクションを実行可能な形式で取得します。
     *
     * @return 実行可能なアクション。
     */
    Executable asExecutable();

    /**
     * アクションを監視可能な形式で取得します。
     *
     * @return 監視可能なアクション。
     */
    Watchable asWatchable();

    /**
     * アクションを要求可能な形式で取得します。
     *
     * @return 要求可能なアクション。
     */
    Requireable asRequireable();

    /**
     * アクションの所有者を取得します。
     *
     * @return アクションの所有者。
     */
    Plugin getOwner();

    /**
     * アクションの名前を取得します。
     *
     * @return アクションの名前。
     */
    String getName();

    /**
     * アクションが実行可能かどうかを確認します。
     *
     * @return アクションが実行可能な場合はtrue、そうでない場合はfalse。
     */
    boolean isExecutable();

    /**
     * アクションが監視可能かどうかを確認します。
     *
     * @return アクションが監視可能な場合はtrue、そうでない場合はfalse。
     */
    boolean isWatchable();

    /**
     * アクションが要求可能かどうかを確認します。
     *
     * @return アクションが要求可能な場合はtrue、そうでない場合はfalse。
     */
    boolean isRequireable();

    /**
     * アクションのインスタンスを取得します。
     *
     * @return アクションのインスタンス。
     */
    T getInstance();

    /**
     * アクションのクラスを取得します。
     *
     * @return アクションのクラス。
     */
    Class<? extends T> getActionClass();
}
