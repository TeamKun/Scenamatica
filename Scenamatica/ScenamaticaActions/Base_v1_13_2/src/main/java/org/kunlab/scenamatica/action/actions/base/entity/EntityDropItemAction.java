package org.kunlab.scenamatica.action.actions.base.entity;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.types.entity.NMSEntity;
import org.kunlab.scenamatica.nms.types.entity.NMSEntityItem;
import org.kunlab.scenamatica.nms.types.item.NMSItemStack;

import java.util.Collections;
import java.util.List;

@ActionMeta("entity_drop_item")
public class EntityDropItemAction extends AbstractGeneralEntityAction
        implements Executable, Watchable
{
    public static final String OUT_KEY_ITEM = "item";
    public static InputToken<EntitySpecifier<Item>> IN_ITEM =
            ofInput("item", Item.class, EntityItemStructure.class)
                    .validator(ScenarioType.ACTION_EXECUTE, EntitySpecifier::hasStructure, "Item structure is not specified.")
                    .validator(
                            ScenarioType.ACTION_EXECUTE,
                            specifier -> specifier.getTargetStructure() instanceof EntityItemStructure,
                            "item must be an item structure."
                    );

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Entity target = this.selectTarget(ctxt);
        if (!(target instanceof InventoryHolder))
            throw new IllegalArgumentException("Target is not inventory holder.");

        EntityItemStructure itemStructure = (EntityItemStructure) ctxt.input(IN_ITEM).getTargetStructure();
        ItemStack stack = itemStructure.getItemStack().create();

        this.makeOutputs(ctxt, target);

        NMSItemStack nmsItemStack = NMSProvider.getProvider().wrap(stack);
        NMSEntity nmsTarget = NMSProvider.getProvider().wrap(target);
        NMSEntityItem dropped = nmsTarget.dropItem(nmsItemStack, 0.0F);
        if (dropped != null)
            itemStructure.applyTo(dropped.getBukkit());
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(ctxt, event))
            return false;

        EntityDropItemEvent e = (EntityDropItemEvent) event;
        Item item = e.getItemDrop();

        boolean result = ctxt.ifHasInput(IN_ITEM, itemSpecifier -> itemSpecifier.checkMatchedEntity(item));
        if (result)
            this.makeOutputs(ctxt, ((EntityDropItemEvent) event).getEntity(), item);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Entity entity)
    {
        super.makeOutputs(ctxt, entity);
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Entity entity, @NotNull Item item)
    {
        ctxt.output(OUT_KEY_ITEM, item);
        super.makeOutputs(ctxt, entity);
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
