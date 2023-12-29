package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.Collections;
import java.util.List;

public class EntityDropItemAction extends AbstractGeneralEntityAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "entity_drop_item";
    public static InputToken<EntitySpecifier<Item>> IN_ITEM =
            ofInput("item", Item.class, EntityItemStructure.class)
                    .validator(ScenarioType.ACTION_EXECUTE, EntitySpecifier::hasStructure, "Item structure is not specified.")
                    .validator(
                            ScenarioType.ACTION_EXECUTE,
                            specifier -> specifier.getTargetStructure() instanceof EntityItemStructure,
                            "item must be an item structure."
                    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Entity target = this.selectTarget(ctxt);
        if (!(target instanceof InventoryHolder))
            throw new IllegalArgumentException("Target is not inventory holder.");

        EntityItemStructure itemStructure = (EntityItemStructure) ctxt.input(IN_ITEM).getTargetStructure();
        ItemStack stack = itemStructure.getItemStack().create();

        target.getWorld().dropItemNaturally(
                target.getLocation(),
                stack,
                (entity) -> {
                    EntityDropItemEvent event = new EntityDropItemEvent(target, entity);
                    Bukkit.getPluginManager().callEvent(event);
                    if (event.isCancelled())
                        entity.remove();

                    itemStructure.applyTo(entity);
                }
        );
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(ctxt, event))
            return false;

        EntityDropItemEvent e = (EntityDropItemEvent) event;
        Item item = e.getItemDrop();

        return ctxt.ifHasInput(IN_ITEM, itemSpecifier -> itemSpecifier.checkMatchedEntity(item));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityDropItemEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_ITEM);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_ITEM);

        return board;
    }
}
