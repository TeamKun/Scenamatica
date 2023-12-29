package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;

import java.util.Collections;
import java.util.List;

public class PlayerMoveAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_move";
    public static final InputToken<LocationStructure> IN_FROM = ofInput(
            "from",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );
    public static final InputToken<LocationStructure> IN_TO = ofInput(
            "to",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Location toLoc = Utils.assignWorldToLocation(ctxt.input(IN_TO), ctxt.getEngine());
        Player target = selectTarget(ctxt);
        target.teleport(toLoc);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerMoveEvent;
        PlayerMoveEvent e = (PlayerMoveEvent) event;

        return ctxt.ifHasInput(IN_FROM, from -> from.isAdequate(e.getFrom()))
                && ctxt.ifHasInput(IN_TO, to -> to.isAdequate(e.getTo()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerMoveEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_TO);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_TO);
        else
            board.register(IN_FROM);

        return board;
    }
}
