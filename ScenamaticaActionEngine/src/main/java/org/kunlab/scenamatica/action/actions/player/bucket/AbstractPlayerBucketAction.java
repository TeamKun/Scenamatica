package org.kunlab.scenamatica.action.actions.player.bucket;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerBucketEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.player.AbstractPlayerAction;
import org.kunlab.scenamatica.action.utils.VoxelUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.nms.enums.NMSHand;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlayerBucketAction extends AbstractPlayerAction
        implements Watchable
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
    public static final InputToken<NMSHand> IN_HAND = ofEnumInput(
            "hand",
            NMSHand.class
    );
    public static final InputToken<Boolean> IN_EVENT_ONLY = ofInput(
            "eventOnly",
            Boolean.class,
            false
    );

    public static final String KEY_OUT_ITEM = "item";
    public static final String KEY_OUT_BLOCK = "block";
    public static final String KEY_OUT_BLOCK_FACE = "blockFace";
    public static final String KEY_OUT_BUCKET = "bucket";
    public static final String KEY_OUT_HAND = "hand";

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

    protected static ItemStack getBucket(Player player, ActionContext ctxt)
    {
        ItemStack stack;
        if (ctxt.hasInput(IN_BUCKET))
        {
            Material bucket = ctxt.input(IN_BUCKET);
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

    protected static Block getPlaceAt(Player player, ActionContext ctxt)
    {
        Block applyBlock = null;
        if (ctxt.hasInput(IN_BLOCK_CLICKED))
            applyBlock = ctxt.input(IN_BLOCK_CLICKED).apply(ctxt.getEngine(), null);
        else if (ctxt.hasInput(IN_BLOCK))
            applyBlock = ctxt.input(IN_BLOCK).apply(ctxt.getEngine(), null);

        if (applyBlock != null)
            return applyBlock;

        Block block = player.getTargetBlockExact(4);
        if (block == null)
            throw new IllegalArgumentException("No block clicked, please specify the block implicitly: " + player.getName());

        return block;
    }

    protected static BlockFace getDirection(Player player, Block placeAt, ActionContext ctxt)
    {
        if (ctxt.hasInput(IN_BLOCK_FACE))
            return ctxt.input(IN_BLOCK_FACE);
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
                    .validator(
                            b -> b.isPresent(IN_EVENT_ONLY) || !(b.isPresent(IN_BLOCK_CLICKED)
                                    && b.isPresent(IN_BLOCK)),
                            "Cannot specify both block and clickedBlock in the action execution mode if 'eventOnly' is not specified."
                    );
        return board;
    }

    protected void makeOutput(@NotNull ActionContext ctxt, @NotNull PlayerBucketEvent event)
    {
        this.makeOutput(ctxt, event.getPlayer(), event.getItemStack(), event.getBlock(), event.getBlockFace(),
                event.getBucket(), NMSHand.fromEquipmentSlot(event.getHand())
        );
    }

    protected void makeOutput(@NotNull ActionContext ctxt, @NotNull Player player, ItemStack item, Block block,
                              BlockFace face, Material bucket, NMSHand hand)
    {
        ctxt.output(KEY_OUT_ITEM, item);
        ctxt.output(KEY_OUT_BLOCK, block);
        ctxt.output(KEY_OUT_BLOCK_FACE, face);
        ctxt.output(KEY_OUT_BUCKET, bucket);
        ctxt.output(KEY_OUT_HAND, hand);
        super.makeOutputs(ctxt, player);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        PlayerBucketEvent e = (PlayerBucketEvent) event;

        boolean result = ctxt.ifHasInput(IN_ITEM, (item) -> item.isAdequate(e.getItemStack()))
                && ctxt.ifHasInput(IN_BLOCK, (block) -> block.isAdequate(e.getBlockClicked()))
                && ctxt.ifHasInput(IN_BLOCK_CLICKED, (block) -> block.isAdequate(e.getBlockClicked()))
                && ctxt.ifHasInput(IN_BLOCK_FACE, (face) -> face == e.getBlockFace())
                && ctxt.ifHasInput(IN_BUCKET, (bucket) -> bucket == e.getBucket())
                && ctxt.ifHasInput(IN_HAND, (hand) -> hand == NMSHand.fromEquipmentSlot(e.getHand()));

        if (result)
            this.makeOutput(ctxt, e);

        return result;
    }

    protected void enumerateItemUse(@NotNull ActionContext ctxt, Player player, Block block, BlockFace direction, ItemStack stack, Actor actor)
    {
        if (ctxt.input(IN_EVENT_ONLY))
        {
            Block blockClicked = null;
            if (ctxt.hasInput(IN_BLOCK_CLICKED))
                blockClicked = ctxt.input(IN_BLOCK_CLICKED).apply(ctxt.getEngine(), null);
            NMSHand hand = ctxt.orElseInput(IN_HAND, () -> null);
            this.doEventOnlyMode(ctxt, player, block, blockClicked, direction, stack.getType(), stack, hand);
            return;
        }

        this.makeOutput(ctxt, player, stack, block, direction, stack.getType(), NMSHand.MAIN_HAND);
        actor.placeItem(
                block.getLocation(),
                stack,
                direction
        );
    }

    protected abstract void doEventOnlyMode(@NotNull ActionContext ctxt, Player who, Block block, Block blockClicked,
                                            BlockFace blockFace, Material bucket, ItemStack itemInHand, NMSHand hand);
}
