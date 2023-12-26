package org.kunlab.scenamatica.action.actions.player;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.TextUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Collections;
import java.util.List;

public class PlayerKickAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_kick";
    public static final InputToken<String> IN_LEAVE_MESSAGE = ofInput(
            "leaveMessage",
            String.class
    );
    public static final InputToken<String> IN_KICK_MESSAGE = ofInput(
            "kickMessage",
            String.class
    );
    public static final InputToken<PlayerKickEvent.Cause> IN_CAUSE = ofEnumInput(
            "cause",
            PlayerKickEvent.Cause.class
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        // leaveMessage はつかえない。

        Component kickMessage = argument.isPresent(IN_KICK_MESSAGE) ?
                Component.text(argument.get(IN_KICK_MESSAGE)): null;

        Player target = selectTarget(argument, engine);
        if (argument.isPresent(IN_CAUSE))
            target.kick(kickMessage, argument.get(IN_CAUSE));
        else
            target.kick(kickMessage);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        PlayerKickEvent e = (PlayerKickEvent) event;
        Component leaveMessage = e.leaveMessage();
        Component kickMessage = e.reason();
        PlayerKickEvent.Cause cause = e.getCause();

        return argument.ifPresent(IN_LEAVE_MESSAGE, message -> TextUtils.isSameContent(leaveMessage, message))
                && argument.ifPresent(IN_KICK_MESSAGE, message -> TextUtils.isSameContent(kickMessage, message))
                && argument.ifPresent(IN_CAUSE, c -> c == cause);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerKickEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_KICK_MESSAGE, IN_CAUSE);

        if (type != ScenarioType.ACTION_EXECUTE)
            board.register(IN_LEAVE_MESSAGE);

        return board;
    }
}
