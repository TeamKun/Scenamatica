package org.kunlab.scenamatica.nms;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.kunlab.scenamatica.nms.types.NMSMinecraftServer;
import org.kunlab.scenamatica.nms.types.NMSWorldServer;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSLivingEntity;

/**
 * NMS にアクセスするためのラッパーを提供します。
 */
public interface WrapperProvider
{
    NMSEntity wrap(Entity bukkitEntity);

    NMSLivingEntity wrap(LivingEntity bukkitEntity);

    NMSWorldServer wrap(World bukkitWorld);

    NMSMinecraftServer wrap(Server server);
}
