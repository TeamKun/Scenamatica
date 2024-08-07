package org.kunlab.scenamatica.action.actions.base.server;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.utils.PlayerLikeCommandSenders;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Action("command_dispatch")
@ActionDoc(
        name = "コマンドの実行",
        description = "コマンドを実行します。",
        events = {
                ServerCommandEvent.class,
                PlayerCommandPreprocessEvent.class
        },

        executable = "コマンドを実行します。",
        expectable = "コマンドが実行されることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = CommandDispatchAction.KEY_OUT_COMMAND,
                        description = "コマンドです。",
                        type = String.class
                ),
                @OutputDoc(
                        name = CommandDispatchAction.KEY_OUT_SENDER,
                        description = "送信者です。",
                        type = PlayerSpecifier.class
                )
        }
)
public class CommandDispatchAction extends AbstractServerAction
        implements Executable, Expectable
{
    @InputDoc(
            name = "command",
            description = "実行するコマンドです。",
            type = String.class,
            admonitions = {
                    @Admonition(
                           type = AdmonitionType.TIP,
                            content = "`/` 接頭辞の有無はどちらでもよく, それほど重要ではありません。"
                    )
            }
    )
    public static final InputToken<String> IN_COMMAND = ofInput(
            "command",
            String.class
    );
    @InputDoc(
            name = "sender",
            description = "送信者です。",
            type = PlayerSpecifier.class,
            admonitions = {
                @Admonition(
                        type = AdmonitionType.INFORMATION,
                        title = "コンソールを指定しますか？",
                        content = "コンソールを実行者として指定する場合は, プレイヤ指定子の代わりに `<CONSOLE>` を指定します。"
                )
        }
    )
    public static final InputToken<PlayerSpecifier> IN_SENDER = ofInput(
            "sender",
            PlayerSpecifier.class,
            ofPlayer()
    );
    public static final String KEY_OUT_COMMAND = "command";
    public static final String KEY_OUT_SENDER = "sender";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        CommandSender sender = PlayerLikeCommandSenders.getCommandSenderOrThrow(
                ctxt.orElseInput(IN_SENDER, () -> null),
                ctxt.getContext()
        );

        String command = ctxt.input(IN_COMMAND);
        if (command.startsWith("/")) // シンタックスシュガーのために, / から始まるやつにも対応
            command = command.substring(1);

        this.outputResults(ctxt, sender, command);
        Bukkit.dispatchCommand(sender, command);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        String command;
        CommandSender sender;
        if (event instanceof ServerCommandEvent)  // non-player
        {
            command = ((ServerCommandEvent) event).getCommand();
            sender = ((ServerCommandEvent) event).getSender();
        }
        else if (event instanceof PlayerCommandPreprocessEvent)  // player
        {
            command = ((PlayerCommandPreprocessEvent) event).getMessage();
            sender = ((PlayerCommandPreprocessEvent) event).getPlayer();
        }
        else
            return false;


        boolean result = ctxt.ifHasInput(
                IN_COMMAND,
                cmd -> Pattern.compile(cmd).matcher(command).matches()
        ) && ctxt.ifHasInput(
                IN_SENDER,
                s -> PlayerLikeCommandSenders.isSpecifiedSender(sender, s)
        );

        if (result)
            this.outputResults(ctxt, sender, command);
        return result;
    }

    private void outputResults(@NotNull ActionContext ctxt, @NotNull CommandSender player, @NotNull String command)
    {
        ctxt.output(KEY_OUT_COMMAND, command);
        ctxt.output(KEY_OUT_SENDER, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Arrays.asList(
                ServerCommandEvent.class,
                PlayerCommandPreprocessEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_COMMAND, IN_SENDER);
        if (type == ScenarioType.ACTION_EXECUTE)
            board = board.requirePresent(IN_COMMAND);

        return board;
    }
}
