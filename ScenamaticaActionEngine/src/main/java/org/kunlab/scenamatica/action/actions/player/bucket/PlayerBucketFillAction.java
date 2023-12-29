package org.kunlab.scenamatica.action.actions.player.bucket;

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
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;

import java.util.Collections;
import java.util.List;

public class PlayerBucketFillAction extends AbstractPlayerBucketAction
        implements Watchable, Executable
{
    public static final String KEY_ACTION_NAME = "player_bucket_fill";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        ItemStack stack = getBucket(player, ctxt);
        Block block = getPlaceAt(player, ctxt);
        BlockFace direction = getDirection(player, block, ctxt);
        Actor actor = ctxt.getActorOrThrow(player);

        if (isFilledBucket(stack.getType()))
            throw new IllegalArgumentException("The bucket is filled with liquid: " + stack.getType() + " held by " + player.getName());

        if (ctxt.input(IN_EVENT_ONLY))
        {
            Block blockClicked = null;
            if (ctxt.hasInput(IN_BLOCK_CLICKED))
                blockClicked = ctxt.input(IN_BLOCK_CLICKED).apply(ctxt.getEngine(), null);
            EquipmentSlot hand = ctxt.orElseInput(IN_HAND, () -> null);
            this.doEventOnlyMode(player, block, blockClicked, direction, stack.getType(), stack, hand);
        }

        actor.placeItem(
                block.getLocation(),
                stack,
                direction
        );
    }

    private void doEventOnlyMode(Player who, Block block, Block blockClicked, BlockFace blockFace, Material bucket, ItemStack itemInHand, EquipmentSlot hand)
    {
        PlayerBucketFillEvent event = new PlayerBucketFillEvent(who, block, blockClicked, blockFace, bucket, itemInHand, hand);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        Material bucketItem = convertLiquidToBucket(block.getType());
        if (bucketItem == null)
            throw new IllegalArgumentException("Cannot convert liquid " + block.getType() + " to bucket.");

        int PLAYER_INVENTORY_MAX = 36;
        for (int i = 0; i < PLAYER_INVENTORY_MAX; i++)
        {
            ItemStack item = who.getInventory().getItem(i);
            if (item == null)
                continue;

            if (item.getType() == bucket)
            {
                who.getInventory().setItem(i, new ItemStack(bucketItem));
                break;
            }
        }

        block.setType(Material.AIR);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerBucketFillEvent.class
        );
    }
}
