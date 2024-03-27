package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

@ActionMeta("player_level_change")
public class PlayerLevelChangeAction extends AbstractPlayerAction
        implements Watchable, Executable,
        Requireable
{
    public static final InputToken<Integer> IN_OLD_LEVEL = ofInput(
            "oldLevel",
            Integer.class
    );
    public static final InputToken<Integer> IN_NEW_LEVEL = ofInput(
            "level",
            Integer.class
    );
    public static final String KEY_OUT_OLD_LEVEL = "oldLevel";
    public static final String KEY_OUT_NEW_LEVEL = "level";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        int oldLevel = player.getLevel();
        int newLevel = ctxt.input(IN_NEW_LEVEL);

        this.makeOutputs(ctxt, player, oldLevel, newLevel);
        player.setLevel(newLevel);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        PlayerLevelChangeEvent e = (PlayerLevelChangeEvent) event;

        boolean result = ctxt.ifHasInput(IN_OLD_LEVEL, oldLevel -> oldLevel == e.getOldLevel())
                && ctxt.ifHasInput(IN_NEW_LEVEL, newLevel -> newLevel == e.getNewLevel());
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getOldLevel(), e.getNewLevel());

        return result;
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
        boolean result = ctxt.ifHasInput(IN_NEW_LEVEL, newLevel -> newLevel == player.getLevel());
        if (result)
            this.makeOutputs(ctxt, player, null, player.getLevel());

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @Nullable Integer oldLevel, int newLevel)
    {
        if (oldLevel != null)
            ctxt.output(KEY_OUT_OLD_LEVEL, oldLevel);
        ctxt.output(KEY_OUT_NEW_LEVEL, newLevel);
        super.makeOutputs(ctxt, player);
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
