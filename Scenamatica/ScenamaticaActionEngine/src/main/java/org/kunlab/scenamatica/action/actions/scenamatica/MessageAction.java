package org.kunlab.scenamatica.action.actions.scenamatica;

import net.kunmc.lab.peyangpaperutils.lib.components.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.events.actor.ActorMessageReceiveEvent;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;

import java.util.Collections;
import java.util.List;

@Action("message")
@ActionDoc(
        name = "プレイヤのメッセージの受信",
        description = "プレイヤのメッセージの受信に関するアクションです。",
        events = {
                ActorMessageReceiveEvent.class
        },

        executable = "プレイヤにメッセージを送信します。",
        watchable = "プレイヤがメッセージを受信することを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = MessageAction.KEY_OUT_MESSAGE,
                        description = "メッセージです。",
                        type = String.class
                ),
                @OutputDoc(
                        name = MessageAction.KEY_OUT_RECIPIENT,
                        description = "メッセージの受信者です。",
                        type = Player.class
                )
        }
)
public class MessageAction extends AbstractScenamaticaAction
        implements Executable, Watchable
{
    @InputDoc(
            name = "message",
            description = "送信するメッセージです。",
            type = String.class,
            admonitions = {
                    @Admonition(
                            type = AdmonitionType.INFORMATION,
                            content = "メッセージの色及び書式は 接頭辞 `§` に続けて[特定の文字](https://minecraft.fandom.com/ja/wiki/Formatting_codes)を指定します。"
                    )
            }
    )
    public static final InputToken<String> IN_MESSAGE = ofInput(
            "message",
            String.class
    );
    @InputDoc(
            name = "recipient",
            description = "メッセージの受信者です。",
            type = PlayerSpecifier.class
    )
    public static final InputToken<PlayerSpecifier> IN_RECIPIENT = ofInput(
            "recipient",
            PlayerSpecifier.class,
            ofPlayer()
    );

    public static final String KEY_OUT_MESSAGE = "message";
    public static final String KEY_OUT_RECIPIENT = "recipient";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player recipient = ctxt.input(IN_RECIPIENT).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalStateException("Cannot select target for this action, please specify target with valid specifier."));
        String message = ctxt.input(IN_MESSAGE);

        this.makeOutputs(ctxt, recipient, message);
        recipient.sendMessage(message);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        assert event instanceof ActorMessageReceiveEvent;
        ActorMessageReceiveEvent e = (ActorMessageReceiveEvent) event;

        Text message = e.getMessage();
        boolean result = ctxt.ifHasInput(IN_MESSAGE, message::isSameContent)
                && ctxt.ifHasInput(IN_RECIPIENT, player -> player.checkMatchedPlayer(e.getPlayer()));
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), message.toPlainText());

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull String message)
    {
        ctxt.output(KEY_OUT_MESSAGE, message);
        ctxt.output(KEY_OUT_RECIPIENT, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                ActorMessageReceiveEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_MESSAGE, IN_RECIPIENT);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_MESSAGE, IN_RECIPIENT);
        return board;
    }
}
