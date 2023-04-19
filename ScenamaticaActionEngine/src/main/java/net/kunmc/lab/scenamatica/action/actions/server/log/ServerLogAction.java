package net.kunmc.lab.scenamatica.action.actions.server.log;

import lombok.Value;
import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class ServerLogAction extends AbstractAction<ServerLogAction.ServerLogActionArgument>
{
    public static final String KEY_ACTION_NAME = "server_log";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@Nullable ServerLogActionArgument argument)
    {
        argument = this.requireArgsNonNull(argument);

        LogRecord record = new LogRecord(
                argument.getLevel() == null ? Level.INFO: argument.getLevel(),
                argument.getMessage()
        );

        record.setSourceClassName("net.kunmc.lab.scenamatica.action.actions.server.log.ServerLogAction");
        record.setSourceMethodName("execute");
        record.setLoggerName(argument.getSource() == null ? "ScenarioEngine": argument.getSource());

        Bukkit.getLogger().log(record);
    }

    @Override
    public boolean isFired(@NotNull ServerLogActionArgument argument, @NotNull Plugin plugin, @NotNull Event event)
    {
        assert event instanceof ServerLogEvent;

        ServerLogEvent serverLogEvent = (ServerLogEvent) event;

        String sender = serverLogEvent.getSender();
        String message = serverLogEvent.getMessage();

        if (argument.getSource() != null && !argument.getSource().equalsIgnoreCase(sender))
            return false;

        if (argument.getLevel() != null && !argument.getLevel().getName().equalsIgnoreCase(serverLogEvent.getLevel().getName()))
            return false;

        return message.matches(argument.getMessage());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                ServerLogEvent.class
        );
    }

    private static String normalizeLevelName(String original)
    {
        String result = original;
        if (original == null)
            return null;

        result = result.toUpperCase();

        if (result.equals("WARN"))
            result = "WARNING";

        if (result.equals("OFF") || result.equals("ALL"))
            throw new IllegalArgumentException("Illegal log level: " + original + " is not allowed here.");

        return result;
    }

    @Override
    public ServerLogActionArgument deserializeArgument(@NotNull Map<String, Object> map)
    {
        MapUtils.checkType(map, ServerLogActionArgument.KEY_MESSAGE, String.class);

        String source = String.valueOf(map.get(ServerLogActionArgument.KEY_SOURCE));
        String message = String.valueOf(map.get(ServerLogActionArgument.KEY_MESSAGE));

        Level level = null;
        if (map.containsKey(ServerLogActionArgument.KEY_LEVEL))
            try
            {
                level = Level.parse(normalizeLevelName(String.valueOf(map.get(ServerLogActionArgument.KEY_LEVEL))));
            }
            catch (IllegalArgumentException e)
            {
                throw new IllegalArgumentException("Invalid log level: " + map.get(ServerLogActionArgument.KEY_LEVEL));
            }

        return new ServerLogActionArgument(source, level, message);
    }

    @Value
    public static class ServerLogActionArgument implements ActionArgument
    {
        public static final String KEY_SOURCE = "source";
        public static final String KEY_LEVEL = "level";
        public static final String KEY_MESSAGE = "message";

        @Nullable
        String source;
        @Nullable
        Level level;
        @NotNull
        String message; // 正規表現になるかもしれない

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof ServerLogActionArgument))
                return false;

            ServerLogActionArgument other = (ServerLogActionArgument) argument;

            return Objects.equals(this.source, other.source) &&
                    this.level == other.level &&
                    Objects.equals(this.message, other.message);
        }
    }
}
