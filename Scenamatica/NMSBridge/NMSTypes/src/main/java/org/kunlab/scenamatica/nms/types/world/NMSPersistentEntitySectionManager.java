package org.kunlab.scenamatica.nms.types.world;

import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.nms.NMSElement;
import org.kunlab.scenamatica.nms.Versioned;

/**
 * パーシステントエンティティセクションマネージャを提供します。
 * @param <E> エンティティの型
 */
@Versioned(from = "1.17")
public interface NMSPersistentEntitySectionManager<E extends Entity> extends NMSElement
{
    /**
     * entity のトラッキングを開始します。
     * @param entity トラッキングするエンティティ
     */
    void startTracking(E entity);

    /**
     * entity のチック経過を開始します。
     * @param entity チック経過を開始するエンティティ
     */
    void startTicking(E entity);
}
