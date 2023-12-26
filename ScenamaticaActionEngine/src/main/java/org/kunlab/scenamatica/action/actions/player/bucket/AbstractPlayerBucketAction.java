package org.kunlab.scenamatica.action.actions.player.bucket;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.player.AbstractPlayerAction;
import org.kunlab.scenamatica.action.utils.VoxelUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlayerBucketAction extends AbstractPlayerAction
{
    public static final InputToken<ItemStackStructure> IN_ITEM = ofInput(
            "item",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );
    public static final InputToken<BlockStructure> IN_BLOCK = ofInput(
            "block",
            BlockStructure.class,
            ofDeserializer(BlockStructure.class)
    );
    public static final InputToken<BlockStructure> IN_BLOCK_CLICKED = ofInput(
            "clickedBlock",
            BlockStructure.class,
            ofDeserializer(BlockStructure.class)
    );
    public static final InputToken<BlockFace> IN_BLOCK_FACE = ofEnumInput(
            "blockFace",
            BlockFace.class
    );
    public static final InputToken<Material> IN_BUCKET = ofEnumInput(
            "bucket",
            Material.class
    );
    public static final InputToken<EquipmentSlot> IN_HAND = ofEnumInput(
            "hand",
            EquipmentSlot.class
    ).validator(
            (slot) -> slot == EquipmentSlot.HAND || slot == EquipmentSlot.OFF_HAND,
            "The hand must be either hand or off hand"
    );
    public static final InputToken<Boolean> IN_EVENT_ONLY = ofInput(
            "eventOnly",
            Boolean.class,
            false
    );

    public static List<? extends AbstractPlayerBucketAction> getActions()
    {
        List<AbstractPlayerBucketAction> actions = new ArrayList<>();

        actions.add(new PlayerBucketEmptyAction());
        actions.add(new PlayerBucketFillAction());

        return actions;
    }

    protected static boolean isEmptyBucket(Material bucket)
    {
        // noinspection deprecation
        return bucket == Material.BUCKET || bucket == Material.LEGACY_BUCKET;
    }

    protected static boolean isFilledBucket(Material bucket)
    {
        return !isEmptyBucket(bucket);
    }

    @Nullable
    protected static Material convertBucketToLiquid(Material bucket)
    {
        switch (bucket)
        {
            case COD_BUCKET:
            case PUFFERFISH_BUCKET:
            case SALMON_BUCKET:
            case TROPICAL_FISH_BUCKET:
            case WATER_BUCKET:
                return Material.WATER;
            case LAVA_BUCKET:
            case LEGACY_LAVA_BUCKET:
                return Material.LAVA;
            default:
                return null;
        }
    }

    @Nullable
    protected static EntityType convertBucketToEntity(Material bucket)
    {
        switch (bucket)
        {
            case COD_BUCKET:
                return EntityType.COD;
            case PUFFERFISH_BUCKET:
                return EntityType.PUFFERFISH;
            case SALMON_BUCKET:
                return EntityType.SALMON;
            case TROPICAL_FISH_BUCKET:
                return EntityType.TROPICAL_FISH;
            default:
                return null;
        }
    }

    @Nullable
    protected static Material convertLiquidToBucket(Material liquid)
    {
        switch (liquid)
        {
            case WATER:
                return Material.WATER_BUCKET;
            case LAVA:
                return Material.LAVA_BUCKET;
            default:
                return null;
        }
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

    protected static ItemStack getBucket(Player player, InputBoard argument)
    {
        ItemStack stack;
        if (argument.isPresent(IN_BUCKET))
        {
            Material bucket = argument.get(IN_BUCKET);
            stack = new ItemStack(bucket);
            if (!isBucketMaterial(stack.getType()))
                throw new IllegalArgumentException("Item " + bucket + " is not a bucket.");
            player.getInventory().setItemInMainHand(stack);
        }
        else
        {
            stack = player.getInventory().getItemInMainHand();
            if (!isBucketMaterial(stack.getType()))
                throw new IllegalArgumentException("No bucket in the main hand, " + "please specify the bucket item implicitly: " + player.getName());
        }

        return stack;
    }

    protected static Block getPlaceAt(Player player, InputBoard argument, ScenarioEngine engine)
    {
        Block applyBlock = null;
        if (argument.isPresent(IN_BLOCK_CLICKED))
            applyBlock = argument.get(IN_BLOCK_CLICKED).apply(engine, null);
        else if (argument.isPresent(IN_BLOCK))
            applyBlock = argument.get(IN_BLOCK).apply(engine, null);

        if (applyBlock != null)
            return applyBlock;

        Block block = player.getTargetBlockExact(4);
        if (block == null)
            throw new IllegalArgumentException("No block clicked, please specify the block implicitly: " + player.getName());

        return block;
    }

    protected static BlockFace getDirection(Player player, Block placeAt, InputBoard argument)
    {
        if (argument.isPresent(IN_BLOCK_FACE))
            return argument.get(IN_BLOCK_FACE);
        else
            return VoxelUtils.toFace(player.getEyeLocation(), placeAt.getLocation()).getOppositeFace();
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_ITEM, IN_BLOCK, IN_BLOCK_CLICKED, IN_BLOCK_FACE, IN_BUCKET, IN_HAND);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.register(IN_EVENT_ONLY)
                    .oneOf(IN_BLOCK, IN_BLOCK_CLICKED);

        return board;
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        PlayerBucketEvent e = (PlayerBucketEvent) event;

        return argument.ifPresent(IN_ITEM, (item) -> item.isAdequate(e.getItemStack()))
                && argument.ifPresent(IN_BLOCK, (block) -> block.isAdequate(e.getBlockClicked()))
                && argument.ifPresent(IN_BLOCK_CLICKED, (block) -> block.isAdequate(e.getBlockClicked()))
                && argument.ifPresent(IN_BLOCK_FACE, (face) -> face == e.getBlockFace())
                && argument.ifPresent(IN_BUCKET, (bucket) -> bucket == e.getBucket())
                && argument.ifPresent(IN_HAND, (hand) -> hand == e.getHand());
    }
}
