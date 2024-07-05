package org.kunlab.scenamatica.action.actions.base.entity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityItem;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityLiving;

import java.util.Collections;
import java.util.List;

@Action("entity_pickup_item")
@ActionDoc(
        name = "エンティティによるアイテムの拾い上げ",
        description = "エンティティがアイテムを拾い上げます。",
        events = {
                EntityPickupItemEvent.class
        },

        executable = "エンティティがアイテムを拾い上げます。",
        watchable = "エンティティがアイテムを拾い上げることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = EntityPickupItemAction.OUT_KEY_ITEM,
                        description = "拾い上げられたアイテムです。",
                        type = Item.class
                ),
                @OutputDoc(
                        name = EntityPickupItemAction.OUT_KEY_REMAINING,
                        description = "残りのアイテムの量です。",
                        type = int.class
                )
        }
)
public class EntityPickupItemAction extends AbstractGeneralEntityAction
        implements Executable, Watchable
{
    @InputDoc(
            name = "remaining",
            description = "残りのアイテムの量です。",
            type = int.class,
            availableFor = ActionMethod.WATCH
    )
    public static final InputToken<Integer> IN_REMAINING = ofInput(
            "remaining",
            Integer.class
    );

    @InputDoc(
            name = "item",
            description = "拾い上げられるアイテムです。\n" +
                    "構造体を指定した場合は, 事前にそのアイテムをワールドに出現させます。",
            type = Item.class,
            requiredOn = {ActionMethod.EXECUTE}
    )
    public static final InputToken<EntitySpecifier<Item>> IN_ITEM = ofInput(
            "item",
            Item.class,
            EntityItemStructure.class
    );

    public static final String OUT_KEY_ITEM = "item";
    public static final String OUT_KEY_REMAINING = "remaining";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Entity target = this.selectTarget(ctxt);
        if (!(target instanceof LivingEntity))
            throw new IllegalArgumentException("Target is not living entity.");

        LivingEntity leTarget = (LivingEntity) target;
        if (!leTarget.getCanPickupItems())
            throw new IllegalStateException("The target cannot pickup items (LivingEntity#getCanPickupItems() is false).");

        EntitySpecifier<Item> itemSpecifier = ctxt.input(IN_ITEM);
        Item item;  // TODO: 統一？
        if (itemSpecifier.isSelectable())
            item = itemSpecifier.selectTarget(ctxt.getContext())
                    .orElseThrow(() -> new IllegalStateException("Item is not found."));
        else
        {
            EntityItemStructure itemStructure = (EntityItemStructure) itemSpecifier;

            // 拾う前に, アイテムを落とす必要がある
            item = target.getWorld().dropItemNaturally(
                    target.getLocation(),
                    itemStructure.getItemStack().create()
            );
            itemStructure.applyTo(item);
        }

        boolean isPlayer = target instanceof Player;
        boolean canPlayerPickUp = item.getPickupDelay() != Short.MAX_VALUE;

        if (isPlayer && !item.canMobPickup())
            throw new IllegalStateException("The item cannot be picked up by mobs (Item#canMobPickup() is false).");
        else if (!isPlayer && !canPlayerPickUp)
            throw new IllegalStateException("The item cannot be picked up by players (Item#canPlayerPickup() is false).");

        int amount = ctxt.orElseInput(IN_REMAINING, () -> item.getItemStack().getAmount() - 1);
        // NMS にすら アイテムを拾ったことを検知する API がないので偽造する
        EntityPickupItemEvent event = new EntityPickupItemEvent(leTarget, item, amount);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            throw new IllegalStateException("Item pickup event is cancelled.");

        int quantity = item.getItemStack().getAmount() - amount;
        NMSEntityLiving nmsEntity = NMSProvider.getProvider().wrap(leTarget);
        NMSEntityItem nmsItem = NMSProvider.getProvider().wrap(item);
        nmsEntity.receive(nmsItem, quantity);
        item.getItemStack().setAmount(event.getRemaining());

        if (isPlayer)
        {
            Player player = (Player) target;
            Inventory inventory = player.getInventory();
            inventory.addItem(item.getItemStack());
        }

        if (event.getRemaining() <= 0)
            item.remove();
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(ctxt, event))
            return false;

        EntityPickupItemEvent e = (EntityPickupItemEvent) event;

        boolean result = ctxt.ifHasInput(IN_REMAINING, remaining -> remaining.equals(e.getRemaining()))
                && ctxt.ifHasInput(IN_ITEM, item -> item.checkMatchedEntity(e.getItem()));
        if (result)
            this.makeOutputs(ctxt, e.getEntity(), e.getItem(), e.getRemaining());

        return ctxt.ifHasInput(IN_REMAINING, remaining -> remaining.equals(e.getRemaining()))
                && ctxt.ifHasInput(IN_ITEM, item -> item.checkMatchedEntity(e.getItem()));
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Entity entity, @NotNull Item item, int remaining)
    {
        ctxt.output(OUT_KEY_ITEM, item);
        ctxt.output(OUT_KEY_REMAINING, remaining);
        super.makeOutputs(ctxt, entity);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityPickupItemEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_REMAINING, IN_ITEM);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_ITEM)
                    .validator(
                            b -> b.ifPresent(
                                    IN_ITEM,
                                    item -> !item.hasStructure() || item.getTargetStructure() instanceof EntityItemStructure
                            ),
                            "Item must be EntityItemStructure."
                    );

        return board;
    }
}
