package org.kunlab.scenamatica.action.actions.extended_v1_16.player.bucket;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.base.player.bucket.AbstractPlayerBucketAction;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.nms.enums.NMSHand;

import java.util.Collections;
import java.util.List;

@Action(value = "player_bucket_empty", supportsSince = MinecraftVersion.V1_16)
public class PlayerBucketEmptyAction extends org.kunlab.scenamatica.action.actions.base.player.bucket.PlayerBucketEmptyAction
        implements Watchable, Executable
{
    @Override
    protected PlayerBucketEmptyEvent createEvent(Player who, Block block, Block blockClicked, BlockFace blockFace, Material bucket, ItemStack itemInHand, EquipmentSlot handSlot)
    {
        return new PlayerBucketEmptyEvent(who, block, blockClicked, blockFace, bucket, itemInHand, handSlot);
    }
}
