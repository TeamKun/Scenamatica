package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

    public static final String KEY_OUT_LEAVE_MESSAGE = "leaveMessage";
    public static final String KEY_OUT_KICK_MESSAGE = "message";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        // leaveMessage はつかえない。
        String kickMessage = ctxt.orElseInput(IN_KICK_MESSAGE, () -> null);
        Player target = selectTarget(ctxt);

        this.makeOutputs(ctxt, target, null, kickMessage);

        // noinspection deprecation  De-Adventure API
        target.kickPlayer(kickMessage);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        PlayerKickEvent e = (PlayerKickEvent) event;
        // noinspection deprecation  De-Adventure API
        String leaveMessage = e.getLeaveMessage();
        // noinspection deprecation  De-Adventure API
        String kickMessage = e.getReason();

        boolean result = ctxt.ifHasInput(IN_LEAVE_MESSAGE, s -> s.equals(leaveMessage))
                && ctxt.ifHasInput(IN_KICK_MESSAGE, s -> s.equals(kickMessage));
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), leaveMessage, kickMessage);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @Nullable String leaveMessage, @Nullable String kickMessage)
    {
        if (leaveMessage != null)
            ctxt.output(KEY_OUT_LEAVE_MESSAGE, leaveMessage);
        if (kickMessage != null)
            ctxt.output(KEY_OUT_KICK_MESSAGE, kickMessage);
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
                .register(IN_KICK_MESSAGE);

        if (type != ScenarioType.ACTION_EXECUTE)
            board.register(IN_LEAVE_MESSAGE);

        return board;
    }
}
