package org.kunlab.scenamatica.action.actions.inventory;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryStructure;

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
