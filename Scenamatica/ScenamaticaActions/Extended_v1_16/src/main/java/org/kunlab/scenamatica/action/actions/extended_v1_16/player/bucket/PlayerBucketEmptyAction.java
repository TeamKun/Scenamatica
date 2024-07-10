package org.kunlab.scenamatica.action.actions.extended_v1_16.player.bucket;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

@Action(value = "player_bucket_empty", supportsSince = MinecraftVersion.V1_16)
public class PlayerBucketEmptyAction extends org.kunlab.scenamatica.action.actions.base.player.bucket.PlayerBucketEmptyAction
        implements Expectable, Executable
{
    @Override
    protected PlayerBucketEmptyEvent createEvent(Player who, Block block, Block blockClicked, BlockFace blockFace, Material bucket, ItemStack itemInHand, EquipmentSlot handSlot)
    {
        return new PlayerBucketEmptyEvent(who, block, blockClicked, blockFace, bucket, itemInHand, handSlot);
    }
}
