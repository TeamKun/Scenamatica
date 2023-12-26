package org.kunlab.scenamatica.action.actions.player;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.TextUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

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
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Player player = selectTarget(argument, engine);
        if (player.isOnline())
            throw new IllegalStateException("Player is already online.");

        Actor actor = PlayerUtils.getActorOrThrow(engine, player);

        actor.joinServer();
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerJoinEvent;
        PlayerJoinEvent e = (PlayerJoinEvent) event;
        Component message = e.joinMessage();

        return argument.ifPresent(IN_MESSAGE, msg -> TextUtils.isSameContent(message, msg));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerJoinEvent.class
        );
    }

    @Override
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        Optional<Player> playerOptional = argument.get(IN_TARGET).selectTarget(engine.getContext());
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
