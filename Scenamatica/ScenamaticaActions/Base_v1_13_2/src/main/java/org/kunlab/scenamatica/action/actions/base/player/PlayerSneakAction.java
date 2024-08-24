package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerToggleSneakEvent;
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

@Action("player_sneak")
@ActionDoc(
        name = "プレイヤのスニーク",
        description = "プレイヤのスニーク状態を変更します。",
        events = {
                PlayerToggleSneakEvent.class
        },

        executable = "プレイヤのスニーク状態を変更します。",
        expectable = "プレイヤのスニーク状態が変更されることを期待します。",
        requireable = "プレイヤのスニーク状態が指定された値になることを要求します。"
)
public class PlayerSneakAction extends AbstractPlayerAction
        implements Executable, Expectable, Requireable
{
    @InputDoc(
            name = "sneaking",
            description = "スニーク状態を指定します。",
            type = boolean.class
    )
    public static final InputToken<Boolean> IN_SNEAKING = ofInput(
            "sneaking",
            Boolean.class
    );

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        boolean sneaking = ctxt.input(IN_SNEAKING);

        Player player = selectTarget(ctxt);
        // Player#setSneaking は PlayerToggleSneakEvent を呼び出さないので、以下手動で呼び出す。
        PlayerToggleSneakEvent event = new PlayerToggleSneakEvent(player, sneaking);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return;

        super.makeOutputs(ctxt, player);
        player.setSneaking(sneaking);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerToggleSneakEvent;
        PlayerToggleSneakEvent e = (PlayerToggleSneakEvent) event;

        boolean result = ctxt.ifHasInput(IN_SNEAKING, sneaking -> sneaking == e.isSneaking());
        if (result)
            super.makeOutputs(ctxt, e.getPlayer());

        return result;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerToggleSneakEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        return ctxt.ifHasInput(IN_SNEAKING, sneaking -> sneaking == player.isSneaking());
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_SNEAKING);
        if (type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
            board.requirePresent(IN_SNEAKING);

        return board;
    }
}
