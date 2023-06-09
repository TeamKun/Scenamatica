package org.kunlab.scenamatica.action.actions.server;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandDispatchAction extends AbstractServerAction<CommandDispatchAction.Argument>
        implements Watchable<CommandDispatchAction.Argument>
{
    public static final String KEY_ACTION_NAME = "command_dispatch";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable CommandDispatchAction.Argument argument)
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
    public boolean isFired(@NotNull CommandDispatchAction.Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
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

        Pattern pattern = Pattern.compile(argument.command);
        Matcher matcher = pattern.matcher(command);

        return matcher.matches()
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
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        MapUtils.checkType(map, Argument.KEY_COMMAND, String.class);
        MapUtils.checkTypeIfContains(map, Argument.KEY_SENDER, String.class);

        String command = (String) map.get(Argument.KEY_COMMAND);
        String sender = MapUtils.getOrNull(map, Argument.KEY_SENDER);

        return new Argument(command, sender);
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractActionArgument
    {
        public static final String KEY_COMMAND = "command";
        public static final String KEY_SENDER = "sender";

        String command;
        @Nullable
        String sender;

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            return argument instanceof Argument
                    && Objects.equals(this.command, ((Argument) argument).command)
                    && Objects.equals(this.sender, ((Argument) argument).sender);
        }

        @Override
        public String getArgumentString()
        {
            return buildArgumentString(
                    KEY_COMMAND, this.command,
                    KEY_SENDER, this.sender
            );
        }
    }
}
