package org.kunlab.scenamatica.action.actions.server.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.server.AbstractServerAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.events.actions.server.ServerLogEvent;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ServerLogAction extends AbstractServerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "server_log";
    public static final InputToken<String> IN_SOURCE = ofInput(
            "source",
            String.class
    );
    public static final InputToken<Level> IN_LEVEL = ofInput(
            "level",
            Level.class,
            ofTraverser(String.class, ((ser, obj) -> {
                String levelName = normalizeLevelName(obj);
                return Level.getLevel(levelName);
            }))
    ).validator(Objects::nonNull, "Level is not found.");
    public static final InputToken<String> IN_MESSAGE = ofInput(
            "message",
            String.class
    );

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
    public void execute(@NotNull ActionContext ctxt)
    {
        Logger logger = LogManager.getRootLogger();
        if (ctxt.hasInput(IN_SOURCE))
            logger = LogManager.getLogger(ctxt.input(IN_SOURCE));
        Level level = ctxt.orElseInput(IN_LEVEL, () -> Level.INFO);

        logger.log(level, ctxt.input(IN_MESSAGE));
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        assert event instanceof ServerLogEvent;

        ServerLogEvent serverLogEvent = (ServerLogEvent) event;

        String sender = serverLogEvent.getSender();
        String message = serverLogEvent.getMessage();

        return ctxt.ifHasInput(IN_SOURCE, sender::equalsIgnoreCase)
                && ctxt.ifHasInput(IN_LEVEL, level -> level.equals(serverLogEvent.getLevel()))
                && ctxt.ifHasInput(IN_MESSAGE, message::matches);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                ServerLogEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_LEVEL, IN_MESSAGE, IN_SOURCE);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_MESSAGE);

        return board;
    }
}
