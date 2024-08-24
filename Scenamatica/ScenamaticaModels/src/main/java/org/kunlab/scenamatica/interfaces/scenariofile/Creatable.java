package org.kunlab.scenamatica.interfaces.scenariofile;

public interface Creatable<T>
{
    /**
     * この構造をもとに新しいオブジェクトを生成します。
     *
     * @return 新しいオブジェクト
     */
    T create();
}
