package org.kunlab.scenamatica.nms;

import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.kunlab.scenamatica.nms.types.NMSMinecraftServer;
import org.kunlab.scenamatica.nms.types.block.NMSBlockPosition;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityHuman;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityItem;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityPlayer;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;
import org.kunlab.scenamatica.nms.types.world.NMSWorldServer;

/**
 * NMS にアクセスするためのラッパーを提供します。
 */
public interface WrapperProvider
{
    // ==================[ ENTITIES ]==================
    NMSEntity wrap(Entity bukkitEntity);

    NMSEntityLiving wrap(LivingEntity bukkitEntity);

    NMSEntityHuman wrap(HumanEntity bukkitEntity);

    NMSEntityItem wrap(Item bukkitEntity);

    NMSEntityPlayer wrap(Player bukkitEntity);

    // ==================[ ITEMS ]==================

    NMSItemStack wrap(ItemStack bukkitItemStack);

    // ==================[ BLOCK/WORLDS ]==================

    NMSWorldServer wrap(World bukkitWorld);

    NMSBlockPosition wrap(Location bukkitLocation);

    // ==================[ SERVER ]==================

    NMSMinecraftServer wrap(Server server);
}
