package org.kunlab.scenamatica.action.actions.player;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.TextUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class PlayerQuitAction extends AbstractPlayerAction
        implements Executable, Watchable, Requireable
{
    public static final String KEY_ACTION_NAME = "player_quit";
    public static final InputToken<String> IN_QUIT_MESSAGE = ofInput(
            "message",
            String.class
    );
    public static final InputToken<PlayerQuitEvent.QuitReason> IN_QUIT_REASON = ofEnumInput(
            "reason",
            PlayerQuitEvent.QuitReason.class
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        PlayerQuitEvent.QuitReason reason = ctxt.orElseInput(IN_QUIT_REASON, () -> PlayerQuitEvent.QuitReason.KICKED);

        Player target = selectTarget(ctxt);
        Actor targetActor = null;
        if (reason != PlayerQuitEvent.QuitReason.KICKED)
            targetActor = ctxt.getActorOrThrow(target);

        switch (reason)
        {
            case KICKED:
                if (ctxt.hasInput(IN_QUIT_MESSAGE))
                    target.kick(Component.text(ctxt.input(IN_QUIT_MESSAGE)));
                else
                    target.kick(null);
                break;
            case DISCONNECTED:
                targetActor.leaveServer();
                break;
            case TIMED_OUT:
                targetActor.kickTimeout();
                break;
            case ERRONEOUS_STATE:
                targetActor.kickErroneous();
                break;
        }
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Optional<Player> target = ctxt.input(IN_TARGET).selectTarget(ctxt.getContext());
        return !target.isPresent() || !target.get().isOnline();
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerQuitEvent;
        PlayerQuitEvent e = (PlayerQuitEvent) event;

        Component quitMessage = e.quitMessage();
        PlayerQuitEvent.QuitReason quitReason = e.getReason();

        return ctxt.ifHasInput(IN_QUIT_MESSAGE, message -> TextUtils.isSameContent(quitMessage, message))
                && ctxt.ifHasInput(IN_QUIT_REASON, reason -> reason == quitReason);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerQuitEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_QUIT_MESSAGE, IN_QUIT_REASON);

        if (type == ScenarioType.ACTION_EXECUTE)
            board.validator(
                    b -> !b.isPresent(IN_QUIT_MESSAGE) || b.ifPresent(IN_QUIT_REASON, r -> r == PlayerQuitEvent.QuitReason.DISCONNECTED),
                    "Quit message is not allowed when executing player quitting by disconnection."
            );

        return board;
    }
}
