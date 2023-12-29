package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

public class PlayerLevelChangeAction extends AbstractPlayerAction
        implements Watchable, Executable,
        Requireable
{
    public static final String KEY_ACTION_NAME = "player_level_change";
    public static final InputToken<Integer> IN_OLD_LEVEL = ofInput(
            "oldLevel",
            Integer.class
    );
    public static final InputToken<Integer> IN_NEW_LEVEL = ofInput(
            "level",
            Integer.class
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        player.setLevel(ctxt.input(IN_NEW_LEVEL));
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        PlayerLevelChangeEvent e = (PlayerLevelChangeEvent) event;

        return ctxt.ifHasInput(IN_OLD_LEVEL, oldLevel -> oldLevel == e.getOldLevel())
                && ctxt.ifHasInput(IN_NEW_LEVEL, newLevel -> newLevel == e.getNewLevel());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerLevelChangeEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        return ctxt.ifHasInput(IN_NEW_LEVEL, newLevel -> newLevel == player.getLevel());
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_NEW_LEVEL);
        switch (type)
        {
            case ACTION_EXPECT:
                board.register(IN_OLD_LEVEL);
                break;
            case ACTION_EXECUTE:
                board.requirePresent(IN_NEW_LEVEL);
                break;
        }

        return board;
    }
}
