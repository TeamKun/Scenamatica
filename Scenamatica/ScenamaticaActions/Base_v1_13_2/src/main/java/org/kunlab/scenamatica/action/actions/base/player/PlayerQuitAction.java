package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
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
import java.util.Optional;

@Action(value = "player_quit", supportsUntil = MinecraftVersion.V1_16_4)
@ActionDoc(
        name = "プレイヤの退出",
        description = "プレイヤをサーバから退出させます。",
        events = {
                PlayerQuitEvent.class
        },

        executable = "プレイヤをサーバから退出させます。",
        watchable = "プレイヤがサーバから退出することを期待します。",
        requireable = "プレイヤがオフラインであることを要求します。",

        outputs = {
                @OutputDoc(
                        name = PlayerQuitAction.KEY_OUT_QUIT_MESSAGE,
                        description = "プレイヤがサーバから退出したときに表示されるメッセージです。",
                        type = String.class
                )
        }
)
public class PlayerQuitAction extends AbstractPlayerAction
        implements Executable, Watchable, Requireable
{
    @InputDoc(
            name = "message",
            description = "プレイヤがサーバから退出したときに表示されるメッセージを指定します。",
            type = String.class
    )
    public static final InputToken<String> IN_QUIT_MESSAGE = ofInput(
            "message",
            String.class
    );
    public static final String KEY_OUT_QUIT_MESSAGE = "message";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player target = selectTarget(ctxt);
        String quitMessage = ctxt.orElseInput(IN_QUIT_MESSAGE, () -> null);
        this.makeOutputs(ctxt, target, quitMessage);

        target.kickPlayer(quitMessage);
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

        String quitMessage = e.getQuitMessage();
        boolean result = ctxt.ifHasInput(IN_QUIT_MESSAGE, message -> message.equalsIgnoreCase(quitMessage));
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), quitMessage);

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @Nullable String message)
    {
        if (message != null)
            ctxt.output(KEY_OUT_QUIT_MESSAGE, message);
        super.makeOutputs(ctxt, player);
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
                .registerAll(IN_QUIT_MESSAGE);

        if (type == ScenarioType.ACTION_EXECUTE)
            board.validator(
                    b -> !b.isPresent(IN_QUIT_MESSAGE),
                    "Quit message is not allowed when executing player quitting by disconnection."
            );

        return board;
    }
}
