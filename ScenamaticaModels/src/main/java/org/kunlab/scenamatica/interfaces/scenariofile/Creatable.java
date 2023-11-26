package org.kunlab.scenamatica.interfaces.scenariofile;

public interface Creatable<T> extends Mapped<T>
{
    /**
     * この構造をもとに新しいオブジェクトを生成します。
     *
     * @return 新しいオブジェクト
     */
    T create();
}
