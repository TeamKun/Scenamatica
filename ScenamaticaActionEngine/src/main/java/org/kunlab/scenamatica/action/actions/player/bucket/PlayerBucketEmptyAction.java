package org.kunlab.scenamatica.action.actions.player.bucket;

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
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Collections;
import java.util.List;

public class PlayerBucketEmptyAction extends AbstractPlayerBucketAction
        implements Watchable, Executable
{
    public static final String KEY_ACTION_NAME = "player_bucket_empty";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Player player = selectTarget(argument, engine);
        ItemStack stack = getBucket(player, argument);
        Block block = getPlaceAt(player, argument, engine);
        BlockFace direction = getDirection(player, block, argument);
        Actor actor = PlayerUtils.getActorOrThrow(engine, player);

        if (isEmptyBucket(stack.getType()))
            throw new IllegalArgumentException("The bucket is empty: " + stack.getType() + " held by " + player.getName());

        if (argument.get(IN_EVENT_ONLY))
        {
            Block blockClicked = null;
            if (argument.isPresent(IN_BLOCK_CLICKED))
                blockClicked = argument.get(IN_BLOCK_CLICKED).apply(engine, null);
            EquipmentSlot hand = argument.orElse(IN_HAND, () -> null);
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
        PlayerBucketEmptyEvent event = new PlayerBucketEmptyEvent(who, block, blockClicked, blockFace, bucket, itemInHand, hand);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        itemInHand = event.getItemStack();

        Material liquid = convertBucketToLiquid(bucket);
        if (liquid == null)
            throw new IllegalArgumentException("Unknown bucket type: " + bucket + ", this action needs a bucket filled with water or lava.");
        EntityType entityToSpawn = convertBucketToEntity(bucket);

        block.setType(liquid);
        if (entityToSpawn != null)
            block.getWorld().spawnEntity(block.getLocation(), entityToSpawn);

        if (itemInHand != null)
            who.getInventory().setItem(hand, itemInHand);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerBucketEmptyEvent.class
        );
    }
}
