package org.kunlab.scenamatica.action.actions.player.bucket;

import lombok.EqualsAndHashCode;
import lombok.Value;
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
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.BeanUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerBucketEmptyAction extends AbstractPlayerBucketAction<PlayerBucketEmptyAction.Argument>
        implements Watchable<PlayerBucketEmptyAction.Argument>, Executable<PlayerBucketEmptyAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_bucket_empty";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player player = argument.getTarget();
        ItemStack stack = getBucket(player, argument);
        Block block = getPlaceAt(player, argument, engine);
        BlockFace direction = getDirection(player, block, argument);
        Actor actor = PlayerUtils.getActorOrThrow(engine, player);

        if (isEmptyBucket(stack.getType()))
            throw new IllegalArgumentException("The bucket is empty: " + stack.getType() + " held by " + player.getName());

        if (argument.isEventOnly())
        {
            Block blockClicked = null;
            if (argument.getBlockClicked() != null)
                blockClicked = BeanUtils.applyBlockBeanData(engine, argument.getBlockClicked());
            this.doEventOnlyMode(player, block, blockClicked, direction, stack.getType(), stack, argument.getHand());
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
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        return super.checkMatchedBucketEvent(argument, engine, event);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerBucketEmptyEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(Argument.deserialize(map, serializer));
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends BucketActionArgument
    {
        public Argument(BucketActionArgument origin)
        {
            super(origin);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                ensureNotPresent(Argument.KEY_TARGET_PLAYER, this.getTargetSpecifier());

                if (this.getBlockClicked() == null && this.getBlock() == null)
                    throw new IllegalArgumentException("No block to place specified(" + Argument.KEY_BLOCK + " or " + Argument.KEY_BLOCK_CLICKED + " is required).");
            }
        }
    }
}
