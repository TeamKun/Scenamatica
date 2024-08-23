package org.kunlab.scenamatica.interfaces.scenariofile;

public interface Creatable extends Mapped
{
    /**
     * この構造をもとに新しいオブジェクトを生成します。
     *
     * @return 新しいオブジェクト
     */
    Object create();
}
