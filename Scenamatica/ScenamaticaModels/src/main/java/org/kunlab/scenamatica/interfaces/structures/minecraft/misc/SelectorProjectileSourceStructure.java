package org.kunlab.scenamatica.interfaces.structures.minecraft.misc;

import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

/**
 * セレクタで指定されたエンティティの発射源です。
 */
public interface SelectorProjectileSourceStructure extends ProjectileSourceStructure, Structure
{
    /**
     * セレクタ文字列を取得します。
     *
     * @return セレクタ文字列
     */
    String getSelectorString();
}
