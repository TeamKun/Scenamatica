package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

public class PlayerFlightAction extends AbstractPlayerAction
        implements Executable, Requireable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_flight";
    public static final InputToken<Boolean> IN_FLYING = ofInput(
            "flying",
            Boolean.class
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        boolean flying = ctxt.orElseInput(IN_FLYING, () -> true);

        Player player = selectTarget(ctxt);
        super.makeOutputs(ctxt, player);
        PlayerToggleFlightEvent event = new PlayerToggleFlightEvent(player, flying);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        if (!player.getAllowFlight())
            player.setAllowFlight(true);  // IllegalStateException 回避

        player.setFlying(flying);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerToggleFlightEvent;
        PlayerToggleFlightEvent e = (PlayerToggleFlightEvent) event;

        boolean result = ctxt.ifHasInput(IN_FLYING, flying -> flying == e.isFlying());
        if (result)
            super.makeOutputs(ctxt, e.getPlayer());

        return result;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerToggleFlightEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        boolean expectState = ctxt.orElseInput(IN_FLYING, () -> true);

        return selectTarget(ctxt).isFlying() == expectState;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return super.getInputBoard(type)
                .registerAll(IN_FLYING);
    }
}
