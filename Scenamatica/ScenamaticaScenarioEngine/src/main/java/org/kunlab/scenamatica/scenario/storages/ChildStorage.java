package org.kunlab.scenamatica.scenario.storages;

import org.kunlab.scenamatica.interfaces.scenario.SessionStorage;

/**
 * セッション変数を表すインターフェースです。
 */
public interface ChildStorage extends SessionStorage
{
    /**
     * キーを取得します。
     *
     * @return キー
     */
    String getKey();
}
