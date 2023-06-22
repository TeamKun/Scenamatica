package net.kunmc.lab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.scenamatica.action.utils.TextUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.action.Requireable;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.BeanSerializer;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerGameModeAction extends AbstractPlayerAction<PlayerGameModeAction.Argument>
        implements Requireable<PlayerGameModeAction.Argument>
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
        if (!super.isFired(argument, engine, event))
            return false;

        assert event instanceof PlayerGameModeChangeEvent;

        PlayerGameModeChangeEvent e = (PlayerGameModeChangeEvent) event;

        GameMode gameMode = argument.getGameMode();
        PlayerGameModeChangeEvent.Cause cause = argument.getCause();
        String cancelMessage = argument.getCancelMessage();


        return e.getPlayer().getUniqueId().equals(argument.getTarget().getUniqueId()) &&
                e.getNewGameMode() == gameMode &&
                e.getCause() == cause &&
                TextUtils.isSameContent(e.cancelMessage(), cancelMessage);
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
        MapUtils.checkEnumName(map, Argument.KEY_GAME_MODE, GameMode.class);
        MapUtils.checkEnumNameIfContains(map, Argument.KEY_CANCEL_MESSAGE, PlayerGameModeChangeEvent.Cause.class);
        MapUtils.checkTypeIfContains(map, Argument.KEY_CANCEL_MESSAGE, String.class);

        GameMode gameMode = MapUtils.getAsEnum(map, Argument.KEY_GAME_MODE, GameMode.class);
        PlayerGameModeChangeEvent.Cause cause = MapUtils.getAsEnum(map, Argument.KEY_CAUSE, PlayerGameModeChangeEvent.Cause.class);
        String cancelMessage = (String) map.get(Argument.KEY_CANCEL_MESSAGE);

        return new Argument(
                super.deserializeTarget(map),
                gameMode,
                cause,
                cancelMessage
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable PlayerGameModeAction.Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        GameMode gameMode = argument.getGameMode();

        return argument.getTarget().getGameMode() == gameMode;
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable PlayerGameModeAction.Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        switch (type)
        {
            case CONDITION_REQUIRE:
            case ACTION_EXECUTE:
                this.throwIfPresent(Argument.KEY_CAUSE, argument.getCause());
                this.throwIfPresent(Argument.KEY_CANCEL_MESSAGE, argument.getCancelMessage());
                break;

        }
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_GAME_MODE = "gamemode";
        public static final String KEY_CAUSE = "cause";
        public static final String KEY_CANCEL_MESSAGE = "cancelMessage";
        @NotNull
        GameMode gameMode;
        @Nullable
        PlayerGameModeChangeEvent.Cause cause;
        @Nullable
        String cancelMessage;

        public Argument(String target, @NotNull GameMode gameMode, @Nullable PlayerGameModeChangeEvent.Cause cause, @Nullable String cancelMessage)
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
        public String getArgumentString()
        {
            return buildArgumentString(
                    super.getArgumentString(),
                    KEY_GAME_MODE, this.gameMode,
                    KEY_CAUSE, this.cause,
                    KEY_CANCEL_MESSAGE, this.cancelMessage
            );
        }
    }
}
