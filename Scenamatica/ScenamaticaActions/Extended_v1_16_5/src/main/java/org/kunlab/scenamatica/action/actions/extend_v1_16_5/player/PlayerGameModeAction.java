package org.kunlab.scenamatica.action.actions.extend_v1_16_5.player;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.base.player.AbstractPlayerAction;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
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
@OutputDocs({
        @OutputDoc(
                name = PlayerGameModeAction.KEY_CAUSE,
                description = "ゲームモードが変更された原因です。",
                type = PlayerGameModeChangeEvent.Cause.class,
                target = ActionMethod.WATCH
        )
})
public class PlayerGameModeAction extends org.kunlab.scenamatica.action.actions.base.player.PlayerGameModeAction
        implements Executable, Watchable, Requireable
{
    @InputDoc(
            name = "cause",
            description = "ゲームモードが変更された原因を指定します。",
            type = PlayerGameModeChangeEvent.Cause.class,
            availableFor = ActionMethod.WATCH
    )
    public static final InputToken<PlayerGameModeChangeEvent.Cause> IN_CAUSE = ofEnumInput(
            "cause",
            PlayerGameModeChangeEvent.Cause.class
    );

    public static final String KEY_CAUSE = "cause";

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
            this.makeOutputs(ctxt, e.getPlayer(), e.getNewGameMode(), e.getCause());

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull GameMode gameMode, @NotNull PlayerGameModeChangeEvent.Cause cause)
    {
        ctxt.output(KEY_OUT_GAME_MODE, gameMode);
        ctxt.output(KEY_CAUSE, cause);
        super.makeOutputs(ctxt, player);
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type);
        if (type == ScenarioType.ACTION_EXPECT)
            board.register(IN_CAUSE);

        return board;
    }
}
