package org.kunlab.scenamatica.action.actions.player;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
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

    public static final String KEY_OUT_IS_BED = "isBed";
    public static final String KEY_OUT_IS_ANCHOR = "isAnchor";
    public static final String KEY_OUT_LOCATION = "location";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        if (!player.isDead())
            throw new IllegalStateException("Player is not dead");

        if (ctxt.hasInput(IN_LOCATION))
            player.setBedSpawnLocation(Utils.assignWorldToLocation(ctxt.input(IN_LOCATION), ctxt.getEngine()), true);

        super.makeOutputs(ctxt, player);
        player.spigot().respawn();
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        boolean result;
        if (event instanceof PlayerRespawnEvent)
        {
            PlayerRespawnEvent e = (PlayerRespawnEvent) event;

            result = ctxt.ifHasInput(IN_IS_BED, isBed -> isBed == e.isBedSpawn())
                    && ctxt.ifHasInput(IN_IS_ANCHOR, isAnchor -> isAnchor == e.isAnchorSpawn())
                    && ctxt.ifHasInput(IN_LOCATION, loc -> loc.isAdequate(e.getRespawnLocation()));
            if (result)
                this.makeOutputs(ctxt, e.getPlayer(), e.isBedSpawn(), e.isAnchorSpawn(), e.getRespawnLocation());
        }
        else
        {
            assert event instanceof PlayerPostRespawnEvent;
            PlayerPostRespawnEvent e = (PlayerPostRespawnEvent) event;

            result = ctxt.ifHasInput(IN_IS_BED, isBed -> isBed == e.isBedSpawn())
                    && ctxt.ifHasInput(IN_LOCATION, loc -> loc.isAdequate(e.getRespawnedLocation()));
            if (result)
                this.makeOutputs(ctxt, e.getPlayer(), e.isBedSpawn(), false, e.getRespawnedLocation());
        }

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, boolean isBed, boolean isAnchor, @NotNull Location location)
    {
        ctxt.output(KEY_OUT_IS_BED, isBed);
        ctxt.output(KEY_OUT_IS_ANCHOR, isAnchor);
        ctxt.output(KEY_OUT_LOCATION, location);
        super.makeOutputs(ctxt, player);
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull Location location)
    {
        ctxt.output(KEY_OUT_LOCATION, location);
        super.makeOutputs(ctxt, player);
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
        if (type != ScenarioType.ACTION_EXECUTE)
            board.registerAll(IN_IS_BED, IN_IS_ANCHOR);

        return board;
    }
}
