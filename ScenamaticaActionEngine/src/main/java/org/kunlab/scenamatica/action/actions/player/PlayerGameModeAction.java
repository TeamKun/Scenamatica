package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.TextUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

public class PlayerGameModeAction extends AbstractPlayerAction
        implements Executable, Watchable, Requireable
{
    public static final String KEY_ACTION_NAME = "player_gamemode";
    public static final InputToken<GameMode> IN_GAME_MODE = ofEnumInput(
            "gamemode",
            GameMode.class
    );
    public static final InputToken<PlayerGameModeChangeEvent.Cause> IN_CAUSE = ofEnumInput(
            "cause",
            PlayerGameModeChangeEvent.Cause.class
    );
    public static final InputToken<String> IN_CANCEL_MESSAGE = ofInput(
            "cancelMessage",
            String.class
    );

    public static final String KEY_OUT_GAME_MODE = "gamemode";
    public static final String KEY_OUT_CAUSE = "cause";
    public static final String KEY_OUT_CANCEL_MESSAGE = "cancelMessage";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player target = selectTarget(ctxt);
        GameMode gm = ctxt.input(IN_GAME_MODE);

        super.makeOutputs(ctxt, target);
        target.setGameMode(gm);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerGameModeChangeEvent;
        PlayerGameModeChangeEvent e = (PlayerGameModeChangeEvent) event;

        boolean result = ctxt.ifHasInput(IN_GAME_MODE, gameMode -> gameMode == e.getNewGameMode())
                && ctxt.ifHasInput(IN_CAUSE, cause -> cause == e.getCause())
                && ctxt.ifHasInput(IN_CANCEL_MESSAGE, cancelMessage -> TextUtils.isSameContent(e.cancelMessage(), cancelMessage));
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getNewGameMode(), e.getCause(), TextUtils.toString(e.cancelMessage()));

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull GameMode gameMode)
    {
        super.makeOutputs(ctxt, player);
        ctxt.output(KEY_OUT_GAME_MODE, gameMode);
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull GameMode gameMode, @NotNull PlayerGameModeChangeEvent.Cause cause, @Nullable String cancelMessage)
    {
        super.makeOutputs(ctxt, player);
        ctxt.output(KEY_OUT_GAME_MODE, gameMode);
        ctxt.output(KEY_OUT_CAUSE, cause);
        if (cancelMessage != null)
            ctxt.output(KEY_OUT_CANCEL_MESSAGE, cancelMessage);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerGameModeChangeEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Player targetPlayer = selectTarget(ctxt);

        boolean result = ctxt.ifHasInput(IN_GAME_MODE, gameMode -> targetPlayer.getGameMode() == gameMode);
        if (result)
            this.makeOutputs(ctxt, targetPlayer, targetPlayer.getGameMode());

        return result;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_GAME_MODE);

        switch (type)
        {

            case ACTION_EXECUTE:
                board.requirePresent(IN_GAME_MODE);
                /* fall through */
            case ACTION_EXPECT:
                board.registerAll(IN_CAUSE, IN_CANCEL_MESSAGE);
                break;
        }

        return board;
    }
}
