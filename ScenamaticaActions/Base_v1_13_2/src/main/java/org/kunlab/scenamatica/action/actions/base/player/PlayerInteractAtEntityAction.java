package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;
import org.kunlab.scenamatica.nms.enums.NMSHand;
import org.kunlab.scenamatica.nms.enums.entity.NMSEntityUseAction;

import java.util.Collections;
import java.util.List;

public class PlayerInteractAtEntityAction extends PlayerInteractEntityAction
{
    public static final String KEY_ACTION_NAME = "player_interact_at_entity";
    public static final String OUT_POSITION = "position";
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
    protected void doInteract(ActionContext ctxt, Entity targeTentity, Actor actor, @NotNull NMSHand hand)
    {
        actor.interactEntity(
                targeTentity,
                NMSEntityUseAction.INTERACT_AT,
                hand,
                ctxt.input(IN_POSITION).create()
        );
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkFired(ctxt, event))
            return false;

        PlayerInteractAtEntityEvent e = (PlayerInteractAtEntityEvent) event;
        Vector clickedPosition = e.getClickedPosition();
        Location loc = clickedPosition.toLocation(ctxt.getContext().getStage().getWorld());

        boolean result = ctxt.ifHasInput(IN_POSITION, position -> position.isAdequate(loc));
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getRightClicked(), NMSHand.fromEquipmentSlot(e.getHand()), loc);

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull Entity targetEntity, @NotNull NMSHand hand, @NotNull Location position)
    {
        ctxt.output(OUT_POSITION, position);
        super.makeOutputs(ctxt, player, targetEntity, hand);
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
