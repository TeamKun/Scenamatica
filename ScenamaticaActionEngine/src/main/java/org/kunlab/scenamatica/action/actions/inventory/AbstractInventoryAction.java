package org.kunlab.scenamatica.action.actions.inventory;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.actions.inventory.interact.AbstractInventoryInteractAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryStructure;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractInventoryAction extends AbstractAction
{
    public static final InputToken<InventoryStructure> IN_INVENTORY = ofInput(
            "inventory",
            InventoryStructure.class,
            ofDeserializer(InventoryStructure.class)
    );

    public static List<? extends AbstractInventoryAction> getActions()
    {
        List<AbstractInventoryAction> actions = new ArrayList<>(AbstractInventoryInteractAction.getActions());

        actions.add(new InventoryCloseAction());
        actions.add(new InventoryOpenAction());

        return actions;
    }

    public boolean checkMatchedInventoryEvent(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof InventoryEvent))
            return false;

        InventoryEvent e = (InventoryEvent) event;

        return argument.ifPresent(IN_INVENTORY, inventory -> inventory.isAdequate(e.getInventory()));
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_INVENTORY);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_INVENTORY);

        return board;
    }
}
