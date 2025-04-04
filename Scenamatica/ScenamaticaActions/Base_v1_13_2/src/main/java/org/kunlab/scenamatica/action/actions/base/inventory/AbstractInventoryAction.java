package org.kunlab.scenamatica.action.actions.base.inventory;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.AbstractAction;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.InventoryStructure;

@OutputDocs({
        @OutputDoc(
                name = AbstractInventoryAction.OUT_KEY_INVENTORY,
                description = "対象のインベントリです。",
                type = Inventory.class
        )

})
@Category(
        id = "inventories",
        name = "インベントリ",
        description = "インベントリに関するアクションを提供します。"
)
public abstract class AbstractInventoryAction extends AbstractAction
{
    public static final InputToken<InventoryStructure> IN_INVENTORY = ofInput(
            "inventory",
            InventoryStructure.class,
            ofDeserializer(InventoryStructure.class)
    );
    public static final String OUT_KEY_INVENTORY = "inventory";

    public boolean checkMatchedInventoryEvent(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof InventoryEvent))
            return false;

        InventoryEvent e = (InventoryEvent) event;

        return ctxt.ifHasInput(IN_INVENTORY, inventory -> inventory.isAdequate(e.getInventory()));
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return ofInputs(type, IN_INVENTORY);
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @Nullable Inventory inventory)
    {
        if (inventory != null)
            ctxt.output(OUT_KEY_INVENTORY, inventory);
        ctxt.commitOutput();
    }
}
