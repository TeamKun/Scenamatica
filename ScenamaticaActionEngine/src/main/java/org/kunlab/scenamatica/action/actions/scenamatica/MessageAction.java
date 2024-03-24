package org.kunlab.scenamatica.action.actions.scenamatica;

import net.kunmc.lab.peyangpaperutils.lib.components.Text;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.events.actor.ActorMessageReceiveEvent;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.Collections;
import java.util.List;

/**
 * プレイヤにメッセージを送信する/送信されることを監視するアクション。
 */
@ActionMeta("message")
public class MessageAction extends AbstractScenamaticaAction
        implements Executable, Watchable
{
    public static final InputToken<String> IN_MESSAGE = ofInput(
            "message",
            String.class
    );
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
