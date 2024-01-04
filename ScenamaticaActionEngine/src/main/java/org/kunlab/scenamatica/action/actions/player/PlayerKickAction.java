package org.kunlab.scenamatica.action.actions.player;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
            "message",
            String.class
    );
    public static final InputToken<PlayerKickEvent.Cause> IN_CAUSE = ofEnumInput(
            "cause",
            PlayerKickEvent.Cause.class
    );

    public static final String KEY_OUT_LEAVE_MESSAGE = "leaveMessage";
    public static final String KEY_OUT_KICK_MESSAGE = "message";
    public static final String KEY_OUT_CAUSE = "cause";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        // leaveMessage はつかえない。

        Component kickMessage = ctxt.ifHasInput(IN_KICK_MESSAGE, Component::text, null);
        PlayerKickEvent.Cause cause = ctxt.orElseInput(IN_CAUSE, () -> null);
        Player target = selectTarget(ctxt);

        this.makeOutputs(ctxt, target, null, kickMessage, cause);
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

        boolean result = ctxt.ifHasInput(IN_LEAVE_MESSAGE, message -> TextUtils.isSameContent(leaveMessage, message))
                && ctxt.ifHasInput(IN_KICK_MESSAGE, message -> TextUtils.isSameContent(kickMessage, message))
                && ctxt.ifHasInput(IN_CAUSE, c -> c == cause);
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), leaveMessage, kickMessage, cause);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @Nullable Component leaveMessage, @Nullable Component kickMessage, @Nullable PlayerKickEvent.Cause cause)
    {
        if (leaveMessage != null)
            ctxt.output(KEY_OUT_LEAVE_MESSAGE, TextUtils.toString(leaveMessage));
        if (kickMessage != null)
            ctxt.output(KEY_OUT_KICK_MESSAGE, TextUtils.toString(kickMessage));
        ctxt.output(KEY_OUT_CAUSE, cause);
        super.makeOutputs(ctxt, player);
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
