package org.kunlab.scenamatica.action.actions.player;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.TextUtils;
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

public class PlayerJoinAction extends AbstractPlayerAction
        implements Executable, Watchable, Requireable
{
    // OfflinePlayer を扱うため, 通常の PlayerAction とは違う実装をする。

    public static final String KEY_ACTION_NAME = "player_join";
    public static final InputToken<String> IN_MESSAGE = ofInput(
            "message",
            String.class
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
        if (player.isOnline())
            throw new IllegalStateException("Player is already online.");

        Actor actor = ctxt.getActorOrThrow(player);

        actor.joinServer();
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerJoinEvent;
        PlayerJoinEvent e = (PlayerJoinEvent) event;
        Component message = e.joinMessage();

        return ctxt.ifHasInput(IN_MESSAGE, msg -> TextUtils.isSameContent(message, msg));
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
        return playerOptional.isPresent() && playerOptional.get().isOnline();
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
