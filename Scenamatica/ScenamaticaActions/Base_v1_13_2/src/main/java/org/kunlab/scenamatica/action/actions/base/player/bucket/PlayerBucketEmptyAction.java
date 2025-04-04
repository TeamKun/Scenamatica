package org.kunlab.scenamatica.action.actions.base.player.bucket;

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
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.exceptions.scenario.IllegalScenarioStateException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.nms.enums.NMSHand;

import java.util.Collections;
import java.util.List;

@Action(value = "player_bucket_empty", supportsUntil = MinecraftVersion.V1_15_2)
@ActionDoc(
        name = "プレイヤによるバケツの空化",
        description = "プレイヤがバケツを空にするアクションです。",
        events = {
                PlayerBucketEmptyEvent.class
        },

        supportsUntil = MCVersion.V1_15_2,

        executable = "プレイヤがバケツを空にします。",
        expectable = "プレイヤがバケツを空にすることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        admonitions = {
                @Admonition(
                        type = AdmonitionType.DANGER,
                        content = "NMS の仕様上の都合により、 `block` を用いた液体の場所は `eventOnly` が `true` の場合にのみ有効です。"
                )
        }
)
public class PlayerBucketEmptyAction extends AbstractPlayerBucketAction
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

        if (isEmptyBucket(stack.getType()))
            throw new IllegalScenarioStateException("The bucket is empty: " + stack.getType() + " held by " + player.getName());

        this.enumerateItemUse(ctxt, player, block, direction, stack, actor);
    }

    @Override
    protected void doEventOnlyMode(@NotNull ActionContext ctxt, Player who, Block block, Block blockClicked,
                                   BlockFace blockFace, Material bucket, ItemStack itemInHand, NMSHand hand)
    {
        this.makeOutput(ctxt, who, itemInHand, block, blockFace, bucket, hand);
        EquipmentSlot handSlot = hand == null ? EquipmentSlot.HAND: hand.toEquipmentSlot();

        PlayerBucketEmptyEvent event = this.createEvent(who, block, blockClicked, blockFace, bucket, itemInHand, handSlot);
        Bukkit.getServer().getPluginManager().callEvent(event);

        if (event.isCancelled())
            return;

        itemInHand = event.getItemStack();

        Material liquid = convertBucketToLiquid(bucket);
        if (liquid == null)
            throw new IllegalScenarioStateException("Unknown bucket type: " + bucket + ", this action needs a bucket filled with water or lava.");
        EntityType entityToSpawn = convertBucketToEntity(bucket);

        block.setType(liquid);
        if (entityToSpawn != null)
            block.getWorld().spawnEntity(block.getLocation(), entityToSpawn);

        if (itemInHand != null)
            who.getInventory().setItem(handSlot, itemInHand);

    }

    protected PlayerBucketEmptyEvent createEvent(Player who, Block block, Block blockClicked, BlockFace blockFace, Material bucket, ItemStack itemInHand, EquipmentSlot handSlot)
    {
        return new PlayerBucketEmptyEvent(who, /* block, */ blockClicked, blockFace, bucket, itemInHand, handSlot);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerBucketEmptyEvent.class
        );
    }
}
