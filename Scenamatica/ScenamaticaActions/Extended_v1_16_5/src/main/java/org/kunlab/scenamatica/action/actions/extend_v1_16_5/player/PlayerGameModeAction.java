package org.kunlab.scenamatica.action.actions.extend_v1_16_5.player;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.base.player.AbstractPlayerAction;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

@Action(value = "player_gamemode", supportsSince = MinecraftVersion.V1_16_5)
public class PlayerGameModeAction extends AbstractPlayerAction
        implements Executable, Watchable, Requireable
{
    public static final InputToken<GameMode> IN_GAME_MODE = ofEnumInput(
            "gamemode",
            GameMode.class
    );
    @InputDoc(
            name = "cause",
            description = "ゲームモードが変更された原因を指定します。",
            type = PlayerGameModeChangeEvent.Cause.class
    )
    public static final InputToken<PlayerGameModeChangeEvent.Cause> IN_CAUSE = ofEnumInput(
            "cause",
            PlayerGameModeChangeEvent.Cause.class
    );
    public static final String KEY_OUT_GAME_MODE = "gamemode";

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
                && ctxt.ifHasInput(IN_CAUSE, cause -> cause == e.getCause());
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getNewGameMode());

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull GameMode gameMode)
    {
        super.makeOutputs(ctxt, player);
        ctxt.output(KEY_OUT_GAME_MODE, gameMode);
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull GameMode gameMode, @NotNull PlayerGameModeChangeEvent.Cause cause)
    {
        super.makeOutputs(ctxt, player);
        ctxt.output(KEY_OUT_GAME_MODE, gameMode);
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
                break;
            case ACTION_EXPECT:
                board.register(IN_CAUSE);
                break;
        }

        return board;
    }
}
