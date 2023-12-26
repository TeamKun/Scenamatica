package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;

import java.util.Collections;
import java.util.List;

public class PlayerInteractAtEntityAction extends PlayerInteractEntityAction
{
    public static final String KEY_ACTION_NAME = "player_interact_at_entity";
    public static InputToken<LocationStructure> IN_POSITION = ofInput(
            "position",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    protected void doInteract(InputBoard argument, Entity targeTentity, Actor actor)
    {
        actor.interactEntity(
                targeTentity,
                NMSEntityUseAction.INTERACT_AT,
                argument.orElse(IN_HAND, () -> EquipmentSlot.HAND),
                argument.get(IN_POSITION).create()
        );
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.isFired(argument, engine, event))
            return false;

        PlayerInteractAtEntityEvent e = (PlayerInteractAtEntityEvent) event;
        Vector clickedPosition = e.getClickedPosition();
        Location loc = clickedPosition.toLocation(engine.getContext().getStage().getWorld());

        return argument.ifPresent(IN_POSITION, position -> position.isAdequate(loc));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerInteractAtEntityEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_POSITION);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_POSITION);

        return board;
    }
}
