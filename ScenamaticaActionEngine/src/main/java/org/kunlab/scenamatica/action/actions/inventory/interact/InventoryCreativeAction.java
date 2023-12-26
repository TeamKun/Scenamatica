package org.kunlab.scenamatica.action.actions.inventory.interact;

import org.bukkit.event.Event;
import org.bukkit.event.inventory.InventoryCreativeEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;

import java.util.Collections;
import java.util.List;

public class InventoryCreativeAction extends InventoryClickAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "inventory_creative";
    public static final InputToken<ItemStackStructure> IN_ITEM = ofInput(
            "item",
            ItemStackStructure.class,
            ofDeserializer(ItemStackStructure.class)
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        int slot = argument.get(IN_SLOT);
        Actor actor = PlayerUtils.getActorOrThrow(
                engine,
                argument.get(IN_PLAYER).selectTarget(engine.getContext())
                        .orElseThrow(() -> new IllegalStateException("Target is not found."))
        );

        actor.giveCreativeItem(slot, argument.get(IN_ITEM).create());
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        InventoryCreativeEvent e = (InventoryCreativeEvent) event;

        return super.isFired(argument, engine, event)
                && argument.ifPresent(IN_ITEM, item -> item.isAdequate(e.getCursor()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                InventoryCreativeEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_ITEM);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_ITEM, IN_SLOT);

        return board;
    }
}
