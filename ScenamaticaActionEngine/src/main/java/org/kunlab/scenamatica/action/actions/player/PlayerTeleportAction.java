package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Collections;
import java.util.List;

public class PlayerTeleportAction extends PlayerMoveAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_teleport";
    public static final InputToken<PlayerTeleportEvent.TeleportCause> IN_CAUSE = ofEnumInput(
            "cause",
            PlayerTeleportEvent.TeleportCause.class
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
        PlayerTeleportEvent.TeleportCause cause;
        if (argument.isPresent(IN_CAUSE))
            cause = argument.get(IN_CAUSE);
        else
            cause = PlayerTeleportEvent.TeleportCause.PLUGIN;

        Player player = selectTarget(argument, engine);
        player.teleport(toLoc, cause);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.isFired(argument, engine, event))
            return false;

        assert event instanceof PlayerTeleportEvent;
        PlayerTeleportEvent e = (PlayerTeleportEvent) event;

        return argument.get(IN_CAUSE) == e.getCause();
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerTeleportEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return super.getInputBoard(type)
                .register(IN_CAUSE);
    }
}
