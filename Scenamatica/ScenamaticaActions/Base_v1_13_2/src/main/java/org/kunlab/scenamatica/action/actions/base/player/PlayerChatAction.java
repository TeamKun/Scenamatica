package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")  // そもそも PlayerChatEvent が deprecated なので。
@Action("player_chat")
@ActionDoc(
        name = "プレイヤのチャット",
        description = "プレイヤがチャットを送信します。",
        events = {
                PlayerChatEvent.class
        },

        executable = "プレイヤがチャットを送信します。",
        watchable = "プレイヤがチャットを送信することを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = PlayerChatAction.KEY_OUT_MESSAGE,
                        description = "送信されたメッセージです。",
                        type = String.class
                ),
                @OutputDoc(
                        name = PlayerChatAction.KEY_OUT_FORMAT,
                        description = "送信されたメッセージのフォーマットです。",
                        type = String.class
                )
        }

)
public class PlayerChatAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    @InputDoc(
            name = "message",
            description = "送信するメッセージまたは判定用に使用する正規表現を指定します。",
            type = String.class
    )
    public static final InputToken<String> IN_MESSAGE = ofInput(
            "message",
            String.class
    );
    @InputDoc(
            name = "format",
            description = "送信するメッセージのフォーマットです。",
            type = String.class
    )
    public static final InputToken<String> IN_FORMAT = ofInput(
            "format",
            String.class
    );
    public static final String KEY_OUT_MESSAGE = "message";
    public static final String KEY_OUT_FORMAT = "format";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player p = selectTarget(ctxt);
        String message = ctxt.input(IN_MESSAGE);

        this.makeOutputs(ctxt, p, message, null);
        p.chat(message);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerChatEvent;
        PlayerChatEvent playerChatEvent = (PlayerChatEvent) event;

        boolean result = ctxt.ifHasInput(IN_MESSAGE, playerChatEvent.getMessage()::matches)
                && ctxt.ifHasInput(IN_FORMAT, playerChatEvent.getFormat()::matches);
        if (result)
            this.makeOutputs(ctxt, playerChatEvent.getPlayer(), playerChatEvent.getMessage(), playerChatEvent.getFormat());

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull String message, @Nullable String format)
    {
        ctxt.output(KEY_OUT_MESSAGE, message);
        if (format != null)
            ctxt.output(KEY_OUT_FORMAT, format);
        super.makeOutputs(ctxt, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        //noinspection deprecation
        return Collections.singletonList(
                PlayerChatEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_MESSAGE);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_TARGET);
        else
            board.register(IN_FORMAT);

        return board;
    }

}
