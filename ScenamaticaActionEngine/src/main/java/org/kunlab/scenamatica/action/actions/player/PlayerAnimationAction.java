package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerAnimationEvent;
import org.bukkit.event.player.PlayerAnimationType;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

public class PlayerAnimationAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_animation";
    public static final InputToken<PlayerAnimationType> IN_ANIMATION_TYPE = ofEnumInput(
            "type",
            PlayerAnimationType.class
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

        ctxt.getActorOrThrow(player)
                .playAnimation(ctxt.input(IN_ANIMATION_TYPE));
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerAnimationEvent;
        PlayerAnimationEvent e = (PlayerAnimationEvent) event;

        return ctxt.ifHasInput(IN_ANIMATION_TYPE, type -> type == e.getAnimationType());
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
