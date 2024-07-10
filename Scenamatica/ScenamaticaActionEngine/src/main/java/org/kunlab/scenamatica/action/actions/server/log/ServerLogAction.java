package org.kunlab.scenamatica.action.actions.server.log;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.AbstractAction;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
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

@Action("server_log")
@ActionDoc(
        name = "サーバーログ",
        description = "サーバーログにメッセージを出力します。",
        events = {
                ServerLogEvent.class
        },

        executable = "サーバーログにメッセージを出力します。",
        watchable = "サーバーログにメッセージが出力されることを期待します。",
        requireable = ActionDoc.UNALLOWED

        // TODO: Impl outputs
)
public class ServerLogAction extends AbstractAction
        implements Executable, Watchable
{
    @InputDoc(
            name = "source",
            description = "出力先のロガー名です。",
            type = String.class
    )
    public static final InputToken<String> IN_SOURCE = ofInput(
            "source",
            String.class
    );
    @InputDoc(
            name = "level",
            description = "ログレベルです。",
            type = EnumLogLevel.class,
            admonitions = {
                    @Admonition(
                            type = AdmonitionType.DANGER,
                            content = "`OFF` および `ALL` は指定できません。"
                    )
            }
    )
    public static final InputToken<Level> IN_LEVEL = ofInput(
            "level",
            Level.class,
            ofTraverser(String.class, ((ser, obj) -> {
                String levelName = normalizeLevelName(obj);
                return Level.getLevel(levelName);
            }))
    ).validator(Objects::nonNull, "Level is not found.");

    @InputDoc(
            name = "message",
            description = "出力するメッセージです。",
            type = String.class
    )
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
