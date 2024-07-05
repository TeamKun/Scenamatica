package org.kunlab.scenamatica.action.actions.extended_v1_16.player.bucket;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

@Action(value = "player_bucket_fill", supportsSince = MinecraftVersion.V1_16)
public class PlayerBucketFillAction extends org.kunlab.scenamatica.action.actions.base.player.bucket.PlayerBucketFillAction
        implements Watchable, Executable
{
    @Override
    protected PlayerBucketFillEvent createEvent(@NotNull Player who, @Nullable Block block, @NotNull Block blockClicked, @NotNull BlockFace blockFace, @NotNull Material bucket, @NotNull ItemStack itemInHand, @Nullable EquipmentSlot hand)
    {
        return new PlayerBucketFillEvent(who, block, blockClicked, blockFace, bucket, itemInHand, hand);
    }
}
