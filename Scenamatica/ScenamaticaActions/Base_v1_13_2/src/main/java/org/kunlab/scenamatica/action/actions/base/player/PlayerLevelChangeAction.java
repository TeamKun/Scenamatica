package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerLevelChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

import java.util.Collections;
import java.util.List;

@Action("player_level_change")
@ActionDoc(
        name = "プレイヤのレベル変更",
        description = "プレイヤのレベルを変更します。",
        events = {
                PlayerLevelChangeEvent.class
        },

        executable = "プレイヤのレベルを変更します。",
        expectable = "プレイヤのレベルが変更されることを期待します。",
        requireable = "プレイヤのレベルが指定された値になることを要求します。",

        outputs = {
                @OutputDoc(
                        name = PlayerLevelChangeAction.KEY_OUT_OLD_LEVEL,
                        description = "変更前のレベルです。",
                        type = int.class
                ),
                @OutputDoc(
                        name = PlayerLevelChangeAction.KEY_OUT_NEW_LEVEL,
                        description = "変更後のレベルです。",
                        type = int.class
                )
        }
)
public class PlayerLevelChangeAction extends AbstractPlayerAction
        implements Expectable, Executable,
        Requireable
{
    @InputDoc(
            name = "oldLevel",
            description = "変更前のレベルを指定します。",
            type = int.class,
            min = 0
    )
    public static final InputToken<Integer> IN_OLD_LEVEL = ofInput(
            "oldLevel",
            Integer.class
    );
    @InputDoc(
            name = "level",
            description = "変更後のレベルを指定します。",
            type = int.class,
            min = 0
    )
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
