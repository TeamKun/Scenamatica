package org.kunlab.scenamatica.action.actions.base.player.bucket;

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
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.nms.enums.NMSHand;

import java.util.Collections;
import java.util.List;

@Action(value = "player_bucket_fill", supportsUntil = MinecraftVersion.V1_15_2)
@ActionDoc(
        name = "プレイヤによるバケツの満杯化",
        description = "プレイヤがバケツを満杯にするアクションです。",
        events = {
                PlayerBucketFillEvent.class
        },

        supportsUntil = MCVersion.V1_15_2,

        executable = "プレイヤがバケツを満杯にします。",
        expectable = "プレイヤがバケツを満杯にすることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        admonitions = {
                @Admonition(
                        type = AdmonitionType.WARNING,
                        on = ActionMethod.EXECUTE,
                        content = "`block` または `clickedBlock` のどちらか一方は必須です。"
                ),
                @Admonition(
                        type = AdmonitionType.DANGER,
                        on = ActionMethod.EXECUTE,
                        content = "NMS の仕様上の都合により、 `block` を用いた液体の場所は `eventOnly` が `true` の場合にのみ有効です。"
                )
        }
)
public class PlayerBucketFillAction extends AbstractPlayerBucketAction
        implements Expectable, Executable
{
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

        this.enumerateItemUse(ctxt, player, block, direction, stack, actor);
    }

    @Override
    protected void doEventOnlyMode(@NotNull ActionContext ctxt, Player who, Block block, Block blockClicked, BlockFace blockFace, Material bucket, ItemStack itemInHand, NMSHand hand)
    {
        super.makeOutput(ctxt, who, itemInHand, block, blockFace, bucket, hand);
        PlayerBucketFillEvent event = this.createEvent(who, blockClicked, block, blockFace, bucket, itemInHand, hand.toEquipmentSlot());
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

    protected PlayerBucketFillEvent createEvent(@NotNull Player who, @Nullable Block block, @NotNull Block blockClicked, @NotNull BlockFace blockFace, @NotNull Material bucket, @NotNull ItemStack itemInHand, @Nullable EquipmentSlot hand)
    {
        return new PlayerBucketFillEvent(who, blockClicked, blockFace, bucket, itemInHand, hand);
    }
    
    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerBucketFillEvent.class
        );
    }
}
