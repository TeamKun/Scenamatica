package org.kunlab.scenamatica.action.actions.player;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.TextUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

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
    public void execute(@NotNull ActionContext ctxt)
    {
        // leaveMessage はつかえない。

        Component kickMessage = ctxt.hasInput(IN_KICK_MESSAGE) ?
                Component.text(ctxt.input(IN_KICK_MESSAGE)): null;

        Player target = selectTarget(ctxt);
        if (ctxt.hasInput(IN_CAUSE))
            target.kick(kickMessage, ctxt.input(IN_CAUSE));
        else
            target.kick(kickMessage);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        PlayerKickEvent e = (PlayerKickEvent) event;
        Component leaveMessage = e.leaveMessage();
        Component kickMessage = e.reason();
        PlayerKickEvent.Cause cause = e.getCause();

        return ctxt.ifHasInput(IN_LEAVE_MESSAGE, message -> TextUtils.isSameContent(leaveMessage, message))
                && ctxt.ifHasInput(IN_KICK_MESSAGE, message -> TextUtils.isSameContent(kickMessage, message))
                && ctxt.ifHasInput(IN_CAUSE, c -> c == cause);
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
