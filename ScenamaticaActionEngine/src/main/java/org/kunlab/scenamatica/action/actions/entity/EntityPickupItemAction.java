package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.Collections;
import java.util.List;

public class EntityPickupItemAction extends AbstractGeneralEntityAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "entity_pickup_item";
    public static final InputToken<Integer> IN_REMAINING = ofInput(
            "remaining",
            Integer.class
    );
    public static final InputToken<EntitySpecifier<Item>> IN_ITEM = ofInput(
            "item",
            Item.class,
            EntityItemStructure.class
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Entity target = this.selectTarget(argument, engine);
        if (!(target instanceof LivingEntity))
            throw new IllegalArgumentException("Target is not living entity.");

        LivingEntity leTarget = (LivingEntity) target;
        if (!leTarget.getCanPickupItems())
            throw new IllegalStateException("The target cannot pickup items (LivingEntity#getCanPickupItems() is false).");

        EntitySpecifier<Item> itemSpecifier = argument.get(IN_ITEM);
        Item item;  // TODO: 統一？
        if (itemSpecifier.isSelectable())
            item = itemSpecifier.selectTarget(engine.getContext())
                    .orElseThrow(() -> new IllegalStateException("Item is not found."));
        else
        {
            EntityItemStructure itemStructure = (EntityItemStructure) itemSpecifier;

            // 拾う前に, アイテムを落とす必要がある
            item = target.getWorld().dropItemNaturally(
                    target.getLocation(),
                    itemStructure.getItemStack().create(),
                    itemStructure::applyTo
            );
        }

        boolean isPlayer = target instanceof Player;

        if (isPlayer && !item.canMobPickup())
            throw new IllegalStateException("The item cannot be picked up by mobs (Item#canMobPickup() is false).");
        else if (!isPlayer && !item.canPlayerPickup())
            throw new IllegalStateException("The item cannot be picked up by players (Item#canPlayerPickup() is false).");

        // NMS にすら アイテムを拾ったことを検知する API がないので偽造する
        EntityPickupItemEvent event = new EntityPickupItemEvent(leTarget, item, 0);
        Bukkit.getPluginManager().callEvent(event);

        if (event.isCancelled())
            throw new IllegalStateException("Item pickup event is cancelled.");

        int quantity = item.getItemStack().getAmount() - event.getRemaining();
        leTarget.playPickupItemAnimation(item, quantity);
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
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(argument, engine, event))
            return false;

        EntityPickupItemEvent e = (EntityPickupItemEvent) event;

        return argument.ifPresent(IN_REMAINING, remaining -> remaining.equals(e.getRemaining()))
                && argument.ifPresent(IN_ITEM, item -> item.checkMatchedEntity(e.getItem()));
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
