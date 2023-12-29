package org.kunlab.scenamatica.action.actions.server;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.utils.PlayerLikeCommandSenders;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class CommandDispatchAction extends AbstractServerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "command_dispatch";
    public static final InputToken<String> IN_COMMAND = ofInput(
            "command",
            String.class
    );
    public static final InputToken<PlayerSpecifier> IN_SENDER = ofInput(
            "sender",
            PlayerSpecifier.class,
            ofPlayer()
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

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


        return ctxt.ifHasInput(
                IN_COMMAND,
                cmd -> Pattern.compile(cmd).matcher(command).matches()
        ) && ctxt.ifHasInput(
                IN_SENDER,
                s -> PlayerLikeCommandSenders.isSpecifiedSender(sender, s)
        );
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
