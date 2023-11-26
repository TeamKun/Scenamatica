package org.kunlab.scenamatica.action.actions.server.log;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.action.actions.server.AbstractServerAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.events.actions.server.ServerLogEvent;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ServerLogAction extends AbstractServerAction<ServerLogAction.Argument>
        implements Executable<ServerLogAction.Argument>, Watchable<ServerLogAction.Argument>
{
    public static final String KEY_ACTION_NAME = "server_log";

    private static String normalizeLevelName(String original)
    {
        String result = original;
        if (original == null)
            return null;

        result = result.toUpperCase();

        if (result.equals("OFF") || result.equals("ALL"))
            throw new IllegalArgumentException("Illegal log level: " + original + " is not allowed here.");

        return result;
    }

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable ServerLogAction.Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Logger logger = argument.getSource() == null ? LogManager.getRootLogger(): LogManager.getLogger(argument.getSource());
        Level level = argument.getLevel() == null ? Level.INFO: argument.getLevel();

        logger.log(level, argument.getMessage());
    }

    @Override
    public boolean isFired(@NotNull ServerLogAction.Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        assert event instanceof ServerLogEvent;

        ServerLogEvent serverLogEvent = (ServerLogEvent) event;

        String sender = serverLogEvent.getSender();
        String message = serverLogEvent.getMessage();

        if (argument.getSource() != null && !argument.getSource().equalsIgnoreCase(sender))
            return false;

        if (argument.getLevel() != null && !argument.getLevel().equals(serverLogEvent.getLevel()))
            return false;

        return argument.getMessage() == null || message.matches(argument.getMessage());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                ServerLogEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        String source = (String) map.get(Argument.KEY_SOURCE);
        String message = (String) map.get(Argument.KEY_MESSAGE);

        Level level = null;
        if (map.containsKey(Argument.KEY_LEVEL))
        {
            level = Level.getLevel(normalizeLevelName(String.valueOf(map.get(Argument.KEY_LEVEL))));
            if (level == null)
                throw new IllegalArgumentException("Illegal log level: " + map.get(Argument.KEY_LEVEL));
        }

        return new Argument(source, level, message);
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractActionArgument
    {
        public static final String KEY_SOURCE = "source";
        public static final String KEY_LEVEL = "level";
        public static final String KEY_MESSAGE = "message";

        String source;
        Level level;
        String message; // 正規表現になるかもしれない

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument other = (Argument) argument;

            return Objects.equals(this.source, other.source)
                    && Objects.equals(this.level, other.level)
                    && Objects.equals(this.message, other.message);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                ensureNotPresent(KEY_SOURCE, this.source);
                ensurePresent(KEY_MESSAGE, this.message);
            }
        }

        @Override
        public String getArgumentString()
        {
            return buildArgumentString(
                    KEY_SOURCE, this.source,
                    KEY_LEVEL, this.level,
                    KEY_MESSAGE, this.message
            );
        }
    }
}
