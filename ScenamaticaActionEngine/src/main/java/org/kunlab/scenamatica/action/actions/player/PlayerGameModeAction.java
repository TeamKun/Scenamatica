package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.GameMode;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.TextUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

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

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        selectTarget(argument, engine).setGameMode(argument.get(IN_GAME_MODE));
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerGameModeChangeEvent;
        PlayerGameModeChangeEvent e = (PlayerGameModeChangeEvent) event;

        return argument.ifPresent(IN_GAME_MODE, gameMode -> gameMode == e.getNewGameMode())
                && argument.ifPresent(IN_CAUSE, cause -> cause == e.getCause())
                && argument.ifPresent(IN_CANCEL_MESSAGE, cancelMessage -> TextUtils.isSameContent(e.cancelMessage(), cancelMessage));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerGameModeChangeEvent.class
        );
    }

    @Override
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        return argument.ifPresent(IN_GAME_MODE, gameMode -> selectTarget(argument, engine).getGameMode() == gameMode);
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
