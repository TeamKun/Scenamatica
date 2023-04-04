package net.kunmc.lab.scenamatica.action.actions.server;

import lombok.Value;
import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.action.utils.PlayerUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CommandDispatchAction extends AbstractAction<CommandDispatchAction.CommandDispatchActionArgument>
{
    public static final String KEY_ACTION_NAME = "command_dispatch";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@Nullable CommandDispatchActionArgument argument)
    {
        argument = this.requireArgsNonNull(argument);

        CommandSender sender;
        if (argument.sender == null)
            sender = Bukkit.getConsoleSender();
        else
            sender = PlayerUtils.getPlayerOrThrow(argument.sender);

        String command = argument.command;
        if (command.startsWith("/")) // シンタックスシュガーのために, / から始まるやつにも対応
            command = command.substring(1);

        Bukkit.dispatchCommand(sender, command);
    }

    @Override
    public boolean isFired(@NotNull CommandDispatchActionArgument argument, @NotNull Plugin plugin, @NotNull Event event)
    {
        String command;
        CommandSender sender = null;
        if (event instanceof ServerCommandEvent)  // non-player
            command = ((ServerCommandEvent) event).getCommand();
        else if (event instanceof PlayerCommandPreprocessEvent)  // player
        {
            command = ((PlayerCommandPreprocessEvent) event).getMessage();
            sender = ((PlayerCommandPreprocessEvent) event).getPlayer();
        }
        else

            return false;
        return Objects.equals(command, argument.command)
                && (argument.sender == null || sender != null && StringUtils.equalsIgnoreCase(argument.sender, sender.getName()));
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
    public CommandDispatchActionArgument deserializeArgument(@NotNull Map<String, Object> map)
    {
        MapUtils.checkType(map, CommandDispatchActionArgument.KEY_COMMAND, String.class);
        MapUtils.checkTypeIfContains(map, CommandDispatchActionArgument.KEY_SENDER, String.class);

        String command = (String) map.get(CommandDispatchActionArgument.KEY_COMMAND);
        String sender = MapUtils.getOrNull(map, CommandDispatchActionArgument.KEY_SENDER);

        return new CommandDispatchActionArgument(command, sender);
    }

    @Value
    public static class CommandDispatchActionArgument implements ActionArgument
    {
        public static final String KEY_COMMAND = "command";
        public static final String KEY_SENDER = "sender";

        String command;
        @Nullable
        String sender;

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            return argument instanceof CommandDispatchActionArgument
                    && Objects.equals(this.command, ((CommandDispatchActionArgument) argument).command)
                    && Objects.equals(this.sender, ((CommandDispatchActionArgument) argument).sender);
        }
    }
}
