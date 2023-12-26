package org.kunlab.scenamatica.action.actions.inventory;

import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.Collections;
import java.util.List;

public class InventoryOpenAction extends AbstractInventoryAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "inventory_open";
    public static final InputToken<PlayerSpecifier> IN_PLAYER = ofInput(
            "target",
            PlayerSpecifier.class,
            ofPlayer()
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Player player = argument.get(IN_PLAYER).selectTarget(engine.getContext())
                .orElseThrow(() -> new IllegalStateException("Cannot select target for this action, please specify target with valid specifier."));

        InventoryStructure inventoryStructure = argument.get(IN_INVENTORY);
        Inventory inventory = inventoryStructure.create();

        player.openInventory(inventory);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedInventoryEvent(argument, engine, event))
            return false;

        assert event instanceof InventoryOpenEvent;
        InventoryOpenEvent e = (InventoryOpenEvent) event;
        HumanEntity player = e.getPlayer();
        if (!(player instanceof Player))
            return false;

        return argument.ifPresent(IN_PLAYER, playerSpecifier -> playerSpecifier.checkMatchedPlayer((Player) player));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryOpenEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_PLAYER);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_PLAYER);
        return board;
    }
}
