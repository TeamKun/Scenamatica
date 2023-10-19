package org.kunlab.scenamatica.action.actions.player.bucket;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.player.AbstractPlayerAction;
import org.kunlab.scenamatica.action.utils.VoxelUtils;
import org.kunlab.scenamatica.commons.utils.BeanUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockBean;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlayerBucketAction<A extends BucketActionArgument> extends AbstractPlayerAction<A>
{
    public static List<? extends AbstractPlayerBucketAction<?>> getActions()
    {
        List<AbstractPlayerBucketAction<?>> actions = new ArrayList<>();

        actions.add(new PlayerBucketEmptyAction());

        return actions;
    }

    protected static boolean isEmptyBucket(Material bucket)
    {
        // noinspection deprecation
        return bucket == Material.BUCKET || bucket == Material.LEGACY_BUCKET;
    }

    @SuppressWarnings("deprecation")
    protected static boolean isBucketMaterial(Material bucket)
    {
        switch (bucket)
        {
            case BUCKET:
            case LAVA_BUCKET:
            case WATER_BUCKET:
            case COD_BUCKET:
            case PUFFERFISH_BUCKET:
            case SALMON_BUCKET:
            case TROPICAL_FISH_BUCKET:
            case MILK_BUCKET:
                // Legacy
            case LEGACY_BUCKET:
            case LEGACY_LAVA_BUCKET:
            case LEGACY_WATER_BUCKET:
            case LEGACY_MILK_BUCKET:
                return true;
            default:
                return false;
        }
    }

    protected static ItemStack getBucket(Actor actor, BucketActionArgument argument)
    {
        ItemStack stack;
        if (argument.getBucket() == null)
        {
            stack = actor.getPlayer().getInventory().getItemInMainHand();
            if (!isBucketMaterial(stack.getType()))
                throw new IllegalArgumentException("No bucket in the main hand, " + "please specify the bucket item implicitly: " + actor.getPlayer().getName());
        }
        else
        {
            stack = new ItemStack(argument.getBucket());
            if (!isBucketMaterial(stack.getType()))
                throw new IllegalArgumentException("Item " + argument.getBucket() + " is not a bucket.");
            actor.getPlayer().getInventory().setItemInMainHand(stack);
        }

        return stack;
    }

    protected static Block getPlaceAt(Player player, BucketActionArgument argument, ScenarioEngine engine)
    {
        BlockBean blockCandidate = null;
        if (argument.getBlockClicked() != null)
            blockCandidate = argument.getBlockClicked();
        else if (argument.getBlock() != null)
            blockCandidate = argument.getBlock();

        if (blockCandidate != null)
        {
            Location fixedLoc = Utils.assignWorldToBlockLocation(blockCandidate, engine);
            BeanUtils.applyBlockBeanData(blockCandidate, fixedLoc);

            return fixedLoc.getBlock();
        }

        Block block = player.getTargetBlockExact(4);
        if (block == null)
            throw new IllegalArgumentException("No block clicked, please specify the block implicitly: " + player.getName());

        return block;
    }

    protected static BlockFace getDirection(Player player, Block placeAt, BucketActionArgument argument)
    {
        if (argument.getBlockFace() != null)
            return argument.getBlockFace();

        return VoxelUtils.toFace(player.getEyeLocation(), placeAt.getLocation()).getOppositeFace();
    }

    public boolean checkMatchedBucketEvent(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        PlayerBucketEvent e = (PlayerBucketEvent) event;

        return (argument.getItemStack() == null || BeanUtils.isSame(argument.getItemStack(), e.getItemStack(), true))
                && (argument.getBlock() == null || BeanUtils.isSame(argument.getBlock(), e.getBlock(), engine))
                && (argument.getBlockClicked() == null || BeanUtils.isSame(argument.getBlockClicked(), e.getBlockClicked(), engine))
                && (argument.getBlockFace() == null || argument.getBlockFace() == e.getBlockFace())
                && (argument.getBucket() == null || argument.getBucket() == e.getBucket())
                && (argument.getHand() == null || argument.getHand() == e.getHand());
    }

}
