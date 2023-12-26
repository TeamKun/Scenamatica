package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

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
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        boolean flying = argument.orElse(IN_FLYING, () -> true);

        Player player = selectTarget(argument, engine);
        PlayerToggleFlightEvent event = new PlayerToggleFlightEvent(player, flying);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        if (!player.getAllowFlight())
            player.setAllowFlight(true);  // IllegalStateException 回避

        player.setFlying(flying);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerToggleFlightEvent;
        PlayerToggleFlightEvent e = (PlayerToggleFlightEvent) event;

        return argument.ifPresent(IN_FLYING, flying -> flying == e.isFlying());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerToggleFlightEvent.class
        );
    }

    @Override
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        boolean expectState = argument.orElse(IN_FLYING, () -> true);

        return selectTarget(argument, engine).isFlying() == expectState;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return super.getInputBoard(type)
                .registerAll(IN_FLYING);
    }
}
