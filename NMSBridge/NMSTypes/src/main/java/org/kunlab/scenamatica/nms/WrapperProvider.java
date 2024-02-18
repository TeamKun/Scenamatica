package org.kunlab.scenamatica.nms;

import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSLivingEntity;

/**
 * NMS にアクセスするためのラッパーを提供します。
 */
public interface WrapperProvider
{
    /**
     * 指定された Bukkit のエンティティを NMS のエンティティに変換します。
     *
     * @param bukkitEntity Bukkit のエンティティ
     * @return NMS のエンティティ
     */
    NMSEntity ofEntity(Entity bukkitEntity);

    /**
     * 指定された Bukkit の生きているエンティティを NMS のエンティティに変換します。
     *
     * @param bukkitEntity Bukkit の生きているエンティティ
     * @return NMS のエンティティ
     */
    NMSLivingEntity ofEntity(LivingEntity bukkitEntity);
}
