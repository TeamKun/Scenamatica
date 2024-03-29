package org.kunlab.scenamatica.action.actions.base.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
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
import org.kunlab.scenamatica.interfaces.context.Actor;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ActionMeta("player_join")
public class PlayerJoinAction extends AbstractPlayerAction
        implements Executable, Watchable, Requireable
{
    // OfflinePlayer を扱うため, 通常の PlayerAction とは違う実装をする。

    public static final InputToken<String> IN_MESSAGE = ofInput(
            "message",
            String.class
    );
    public static final String KEY_OUT_MESSAGE = "message";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        if (player.isOnline())
            throw new IllegalStateException("Player is already online.");

        Actor actor = ctxt.getActorOrThrow(player);

        this.makeOutputs(ctxt, player, null);
        actor.joinServer();
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerJoinEvent;
        PlayerJoinEvent e = (PlayerJoinEvent) event;

        // noinspection deprecation  De-Adventure API
        String message = e.getJoinMessage();

        boolean result = ctxt.ifHasInput(IN_MESSAGE, msg -> msg.equals(message));
        if (result)
            this.makeOutputs(ctxt, e.getPlayer(), message);

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @Nullable String message)
    {
        if (message != null)
            ctxt.output(KEY_OUT_MESSAGE, message);
        this.makeOutputs(ctxt, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerJoinEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Optional<Player> playerOptional = ctxt.input(IN_TARGET).selectTarget(ctxt.getContext());
        boolean result = playerOptional.isPresent() && playerOptional.get().isOnline();
        if (result)
            this.makeOutputs(ctxt, playerOptional.get(), null);

        return result;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type);
        if (type != ScenarioType.CONDITION_REQUIRE)
            board.register(IN_MESSAGE);

        return board;
    }
}
