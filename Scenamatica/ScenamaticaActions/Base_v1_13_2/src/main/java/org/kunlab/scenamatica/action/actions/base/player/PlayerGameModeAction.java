package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

import java.util.Collections;
import java.util.List;

@Action(value = "player_gamemode", supportsUntil = MinecraftVersion.V1_16_4)
@ActionDoc(
        name = "プレイヤのゲームモード",
        description = "プレイヤのゲームモードを変更します。",
        events = {
                PlayerGameModeChangeEvent.class
        },

        supportsUntil = MCVersion.V1_16_4,

        executable = "プレイヤのゲームモードを変更します。",
        expectable = "プレイヤのゲームモードが変更されることを期待します。",
        requireable = "プレイヤのゲームモードが指定されたものであることを要求します。",

        outputs = {
                @OutputDoc(
                        name = PlayerGameModeAction.KEY_OUT_GAME_MODE,
                        description = "変更されたゲームモードです。",
                        type = GameMode.class
                )
        }
)
public class PlayerGameModeAction extends AbstractPlayerAction
        implements Executable, Expectable, Requireable
{
    @InputDoc(
            name = "gamemode",
            description = "プレイヤのゲームモードを指定します。",
            type = GameMode.class,
            admonitions = {
                    @Admonition(
                            type = AdmonitionType.DANGER,
                            content = "`gamemode` は上記に示す列挙型のみ指定できます。\n" +
                                    "Minecraft 旧バージョンで使用できるゲームモード ID(`integer`: `0` 〜 `3`)は指定できません。"
                    )
            }
    )
    public static final InputToken<GameMode> IN_GAME_MODE = ofEnumInput(
            "gamemode",
            GameMode.class
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

        boolean result = ctxt.ifHasInput(IN_GAME_MODE, gameMode -> gameMode == e.getNewGameMode());
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getNewGameMode());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull GameMode gameMode)
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

        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_GAME_MODE);

        return board;
    }
}
