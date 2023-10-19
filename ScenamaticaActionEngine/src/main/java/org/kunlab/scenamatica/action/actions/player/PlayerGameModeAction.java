package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.TextUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerGameModeAction extends AbstractPlayerAction<PlayerGameModeAction.Argument>
        implements Executable<PlayerGameModeAction.Argument>, Watchable<PlayerGameModeAction.Argument>, Requireable<PlayerGameModeAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_gamemode";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable PlayerGameModeAction.Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player targetPlayer = argument.getTarget();
        GameMode gameMode = argument.getGameMode();

        targetPlayer.setGameMode(gameMode);
    }

    @Override
    public boolean isFired(@NotNull PlayerGameModeAction.Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerGameModeChangeEvent;

        PlayerGameModeChangeEvent e = (PlayerGameModeChangeEvent) event;

        GameMode expectedMode = argument.getGameMode();
        PlayerGameModeChangeEvent.Cause expectedCause = argument.getCause();
        String expectedCancelMsg = argument.getCancelMessage();


        return (argument.getTargetSpecifier() == null || e.getPlayer().getUniqueId().equals(argument.getTarget().getUniqueId()))
                && (expectedMode == null || e.getNewGameMode() == expectedMode)
                && (expectedCause == null || e.getCause() == expectedCause)
                && (expectedCancelMsg == null || TextUtils.isSameContent(e.cancelMessage(), expectedCancelMsg));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerGameModeChangeEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                super.deserializeTarget(map),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_GAME_MODE, GameMode.class),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_CAUSE, PlayerGameModeChangeEvent.Cause.class),
                (String) map.get(Argument.KEY_CANCEL_MESSAGE)
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable PlayerGameModeAction.Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        GameMode gameMode = argument.getGameMode();

        return argument.getTarget().getGameMode() == gameMode;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_GAME_MODE = "gamemode";
        public static final String KEY_CAUSE = "cause";
        public static final String KEY_CANCEL_MESSAGE = "cancelMessage";

        GameMode gameMode;
        PlayerGameModeChangeEvent.Cause cause;
        String cancelMessage;

        public Argument(String target, GameMode gameMode, PlayerGameModeChangeEvent.Cause cause, String cancelMessage)
        {
            super(target);
            this.gameMode = gameMode;
            this.cause = cause;
            this.cancelMessage = cancelMessage;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg) &&
                    this.gameMode == arg.gameMode &&
                    this.cause == arg.cause &&
                    Objects.equals(this.cancelMessage, arg.cancelMessage);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);
            switch (type)
            {
                case ACTION_EXECUTE:
                    ensureNotPresent(KEY_GAME_MODE, this.gameMode);
                    /* fall through */
                case CONDITION_REQUIRE:
                    ensureNotPresent(KEY_TARGET_PLAYER, this.getTargetSpecifier());
                    ensurePresent(Argument.KEY_CAUSE, this.cause);
                    ensurePresent(Argument.KEY_CANCEL_MESSAGE, this.cancelMessage);
                    break;

            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_GAME_MODE, this.gameMode,
                    KEY_CAUSE, this.cause,
                    KEY_CANCEL_MESSAGE, this.cancelMessage
            );
        }
    }
}
