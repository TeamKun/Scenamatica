package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

@Action("player_animation")
public class PlayerAnimationAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final InputToken<PlayerAnimationType> IN_ANIMATION_TYPE = ofEnumInput(
            "type",
            PlayerAnimationType.class
    );
    public static final String KEY_OUT_ANIMATION_TYPE = "type";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        PlayerAnimationType type = ctxt.input(IN_ANIMATION_TYPE);

        this.makeOutputs(ctxt, player, type);
        ctxt.getActorOrThrow(player)
                .playAnimation(type);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerAnimationEvent;
        PlayerAnimationEvent e = (PlayerAnimationEvent) event;

        boolean result = ctxt.ifHasInput(IN_ANIMATION_TYPE, type -> type == e.getAnimationType());
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), e.getAnimationType());

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull PlayerAnimationType type)
    {
        ctxt.output(KEY_OUT_ANIMATION_TYPE, type);
        super.makeOutputs(ctxt, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerAnimationEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_ANIMATION_TYPE);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_TARGET);

        return board;
    }
}
