package org.kunlab.scenamatica.action.actions.player.bucket;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.inventory.EquipmentSlot;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.player.AbstractPlayerActionArgument;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Map;
import java.util.Objects;

@Getter
@EqualsAndHashCode(callSuper = true)
public class BucketActionArgument extends AbstractPlayerActionArgument
{
    public static final String KEY_ITEM_STACK = "item";
    public static final String KEY_BLOCK = "block";
    public static final String KEY_BLOCK_CLICKED = "clickedBlock";
    public static final String KEY_BLOCK_FACE = "blockFace";
    public static final String KEY_BUCKET = "bucket";
    public static final String KEY_HAND = "hand";
    public static final String KEY_EVENT_ONLY = "eventOnly";

    ItemStackBean itemStack;
    BlockBean block;
    BlockBean blockClicked;
    BlockFace blockFace;
    Material bucket;
    EquipmentSlot hand;
    boolean eventOnly;

    public BucketActionArgument(BucketActionArgument origin)
    {
        this(origin.getTargetSpecifier(), origin.itemStack, origin.block, origin.blockClicked, origin.blockFace, origin.bucket, origin.hand, origin.eventOnly);
    }

    public BucketActionArgument(String target, ItemStackBean itemStack, BlockBean block, BlockBean blockClicked, BlockFace blockFace, Material bucket, EquipmentSlot hand, boolean eventOnly)
    {
        super(target);
        this.itemStack = itemStack;
        this.block = block;
        this.blockClicked = blockClicked;
        this.blockFace = blockFace;
        this.bucket = bucket;
        this.hand = hand;
        this.eventOnly = eventOnly;
    }

    public static BucketActionArgument deserialize(Map<String, Object> map, BeanSerializer serializer)
    {
        ItemStackBean itemStack = null;
        if (map.containsKey(BucketActionArgument.KEY_ITEM_STACK))
            itemStack = serializer.deserializeItemStack(MapUtils.checkAndCastMap(
                    map.get(BucketActionArgument.KEY_ITEM_STACK),
                    String.class,
                    Object.class
            ));

        BlockBean block = null;
        if (map.containsKey(BucketActionArgument.KEY_BLOCK))
            block = serializer.deserializeBlock(MapUtils.checkAndCastMap(
                    map.get(BucketActionArgument.KEY_BLOCK),
                    String.class,
                    Object.class
            ));

        BlockBean blockClicked = null;
        if (map.containsKey(BucketActionArgument.KEY_BLOCK_CLICKED))
            blockClicked = serializer.deserializeBlock(MapUtils.checkAndCastMap(
                    map.get(BucketActionArgument.KEY_BLOCK_CLICKED),
                    String.class,
                    Object.class
            ));

        return new BucketActionArgument(
                (String) map.get(AbstractPlayerActionArgument.KEY_TARGET_PLAYER),
                itemStack,
                block,
                blockClicked,
                MapUtils.getAsEnumOrNull(map, BucketActionArgument.KEY_BLOCK_FACE, BlockFace.class),
                MapUtils.getAsEnumOrNull(map, BucketActionArgument.KEY_BUCKET, Material.class),
                MapUtils.getAsEnumOrNull(map, BucketActionArgument.KEY_HAND, EquipmentSlot.class),
                MapUtils.getOrDefault(map, BucketActionArgument.KEY_EVENT_ONLY, false)
        );
    }

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof BucketActionArgument))
            return false;

        BucketActionArgument arg = (BucketActionArgument) argument;

        return super.isSame(arg)
                && Objects.equals(this.itemStack, arg.itemStack)
                && Objects.equals(this.block, arg.block)
                && Objects.equals(this.blockClicked, arg.blockClicked)
                && this.blockFace == arg.blockFace
                && this.bucket == arg.bucket
                && this.hand == arg.hand;
    }

    @Override
    public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
    {
        super.validate(engine, type);

        if (!(this.block == null || this.blockClicked == null))
            throw new IllegalArgumentException("Both " + KEY_BLOCK + " and " + KEY_BLOCK_CLICKED + " are specified.");
    }

    @Override
    public String getArgumentString()
    {
        return appendArgumentString(
                super.getArgumentString(),
                KEY_ITEM_STACK, this.itemStack,
                KEY_BLOCK, this.block,
                KEY_BLOCK_CLICKED, this.blockClicked,
                KEY_BLOCK_FACE, this.blockFace,
                KEY_BUCKET, this.bucket,
                KEY_HAND, this.hand
        );
    }
}
