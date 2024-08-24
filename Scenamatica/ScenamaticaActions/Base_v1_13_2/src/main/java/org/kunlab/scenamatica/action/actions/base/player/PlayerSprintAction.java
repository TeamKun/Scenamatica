package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;

import java.util.Collections;
import java.util.List;

@Action("player_sprint")
@ActionDoc(
        name = "プレイヤの走り",
        description = "プレイヤを走らせます。",
        events = {
                PlayerToggleSprintEvent.class
        },

        executable = "プレイヤの走り状態を変更します。",
        expectable = "プレイヤの走り状態が変更されることを期待します。",
        requireable = "プレイヤの走り状態が指定された値になることを要求します。"
)
public class PlayerSprintAction extends AbstractPlayerAction
        implements Executable, Expectable, Requireable
{
    @InputDoc(
            name = "sprinting",
            description = "走り状態を指定します。",
            type = boolean.class
    )
    public static final InputToken<Boolean> IN_SPRINTING = ofInput(
            "sprinting",
            Boolean.class
    );

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        boolean sprinting = ctxt.input(IN_SPRINTING);

        Player player = selectTarget(ctxt);
        // Player#setSprinting は PlayerToggleSprintEvent を呼び出さないので、以下手動で呼び出す。
        PlayerToggleSprintEvent event = new PlayerToggleSprintEvent(player, sprinting);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        super.makeOutputs(ctxt, player);
        player.setSprinting(sprinting);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerToggleSprintEvent;
        PlayerToggleSprintEvent e = (PlayerToggleSprintEvent) event;

        boolean result = ctxt.ifHasInput(IN_SPRINTING, sprinting -> sprinting == e.isSprinting());
        if (result)
            super.makeOutputs(ctxt, e.getPlayer());

        return result;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerToggleSprintEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        return ctxt.ifHasInput(IN_SPRINTING, sprinting -> sprinting == player.isSprinting());
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_SPRINTING);
        if (type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
            board.requirePresent(IN_SPRINTING);

        return board;
    }
}
