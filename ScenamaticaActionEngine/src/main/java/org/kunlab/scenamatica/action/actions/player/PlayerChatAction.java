package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Collections;
import java.util.List;

@SuppressWarnings("deprecation")  // そもそも PlayerChatEvent が deprecated なので。
public class PlayerChatAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_chat";
    public static final InputToken<String> IN_MESSAGE = ofInput(
            "message",
            String.class
    );
    public static final InputToken<String> IN_FORMAT = ofInput(
            "format",
            String.class
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Player p = selectTarget(argument, engine);
        p.chat(argument.get(IN_MESSAGE));
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerChatEvent;
        PlayerChatEvent playerChatEvent = (PlayerChatEvent) event;

        return argument.ifPresent(IN_MESSAGE, playerChatEvent.getMessage()::matches)
                && argument.ifPresent(IN_FORMAT, playerChatEvent.getFormat()::matches);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        //noinspection deprecation
        return Collections.singletonList(
                PlayerChatEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_MESSAGE);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_TARGET);
        else
            board.register(IN_FORMAT);

        return board;
    }

}
