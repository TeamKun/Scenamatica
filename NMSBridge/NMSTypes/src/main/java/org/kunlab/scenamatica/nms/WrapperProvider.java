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
    NMSEntity wrap(Entity bukkitEntity);

    NMSLivingEntity wrap(LivingEntity bukkitEntity);
}
