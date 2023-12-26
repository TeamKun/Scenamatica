package org.kunlab.scenamatica.action.actions.player;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;

import java.util.Arrays;
import java.util.List;

public class PlayerRespawnAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_respawn";
    public static final InputToken<Boolean> IN_IS_BED = ofInput(
            "isBed",
            Boolean.class
    );
    public static final InputToken<Boolean> IN_IS_ANCHOR = ofInput(
            "isAnchor",
            Boolean.class
    );
    public static final InputToken<LocationStructure> IN_LOCATION = ofInput(
            "location",
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
        Player player = selectTarget(argument, engine);
        if (!player.isDead())
            throw new IllegalStateException("Player is not dead");

        if (argument.isPresent(IN_LOCATION))
            player.setBedSpawnLocation(Utils.assignWorldToLocation(argument.get(IN_LOCATION), engine), true);

        player.spigot().respawn();
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof PlayerRespawnEvent || super.checkMatchedPlayerEvent(argument, engine, event)))
            return false;

        if (event instanceof PlayerRespawnEvent)
        {
            PlayerRespawnEvent e = (PlayerRespawnEvent) event;

            return argument.ifPresent(IN_IS_BED, isBed -> isBed == e.isBedSpawn())
                    && argument.ifPresent(IN_IS_ANCHOR, isAnchor -> isAnchor == e.isAnchorSpawn())
                    && argument.ifPresent(IN_LOCATION, loc -> loc.isAdequate(e.getRespawnLocation()));
        }
        else
        {
            assert event instanceof PlayerPostRespawnEvent;
            PlayerPostRespawnEvent e = (PlayerPostRespawnEvent) event;

            return argument.ifPresent(IN_IS_BED, isBed -> isBed == e.isBedSpawn())
                    && argument.ifPresent(IN_LOCATION, loc -> loc.isAdequate(e.getRespawnedLocation()));
        }
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Arrays.asList(
                PlayerRespawnEvent.class,
                PlayerPostRespawnEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_LOCATION);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.registerAll(IN_IS_BED, IN_IS_ANCHOR);

        return board;
    }
}
