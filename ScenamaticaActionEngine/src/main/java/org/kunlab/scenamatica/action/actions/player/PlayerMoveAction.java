package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
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
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Location toLoc = Utils.assignWorldToLocation(argument.get(IN_TO), engine);
        Player target = selectTarget(argument, engine);
        target.teleport(toLoc);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerMoveEvent;
        PlayerMoveEvent e = (PlayerMoveEvent) event;

        return argument.ifPresent(IN_FROM, from -> from.isAdequate(e.getFrom()))
                && argument.ifPresent(IN_TO, to -> to.isAdequate(e.getTo()));
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
            board.requirePresent(IN_FROM);
        else
            board.register(IN_FROM);

        return board;
    }
}
