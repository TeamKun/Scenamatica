package net.kunmc.lab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.scenamatica.action.utils.TextUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.action.Requireable;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerGameModeChangeEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class GameModeAction extends AbstractPlayerAction<GameModeAction.GameModeChangeArgument>
        implements Requireable<GameModeAction.GameModeChangeArgument>
{
    public static final String KEY_ACTION_NAME = "player_gamemode";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@Nullable GameModeChangeArgument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player targetPlayer = argument.getTarget();
        GameMode gameMode = argument.getGameMode();
        // PlayerGameModeChangeEvent.Cause cause = argument.getCause();  <= EXECUTE では使用不可。
        // String cancelMessage = argument.getCancelMessage(); <= EXECUTE では使用不可。

        targetPlayer.setGameMode(gameMode);
    }

    @Override
    public boolean isFired(@NotNull GameModeChangeArgument argument, @NotNull Plugin plugin, @NotNull Event event)
    {
        if (!super.isFired(argument, plugin, event))
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
    public GameModeChangeArgument deserializeArgument(@NotNull Map<String, Object> map)
    {
        MapUtils.checkEnumName(map, GameModeChangeArgument.KEY_GAME_MODE, GameMode.class);
        MapUtils.checkEnumNameIfContains(map, GameModeChangeArgument.KEY_CANCEL_MESSAGE, PlayerGameModeChangeEvent.Cause.class);
        MapUtils.checkTypeIfContains(map, GameModeChangeArgument.KEY_CANCEL_MESSAGE, String.class);

        GameMode gameMode = MapUtils.getAsEnum(map, GameModeChangeArgument.KEY_GAME_MODE, GameMode.class);
        PlayerGameModeChangeEvent.Cause cause = MapUtils.getAsEnum(map, GameModeChangeArgument.KEY_CAUSE, PlayerGameModeChangeEvent.Cause.class);
        String cancelMessage = (String) map.get(GameModeChangeArgument.KEY_CANCEL_MESSAGE);

        return new GameModeChangeArgument(
                super.deserializeTarget(map),
                gameMode,
                cause,
                cancelMessage
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable GameModeChangeArgument argument, @NotNull Plugin plugin)
    {
        argument = this.requireArgsNonNull(argument);

        GameMode gameMode = argument.getGameMode();

        return argument.getTarget().getGameMode() == gameMode;
    }

    @Override
    public void validateArgument(@Nullable GameModeChangeArgument argument)
    {
        argument = this.requireArgsNonNull(argument);

        this.throwIfPresent(GameModeChangeArgument.KEY_CAUSE, argument.getCause());
        this.throwIfPresent(GameModeChangeArgument.KEY_CANCEL_MESSAGE, argument.getCancelMessage());
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class GameModeChangeArgument extends AbstractPlayerActionArgument
    {
        public static final String KEY_GAME_MODE = "gameMode";
        public static final String KEY_CAUSE = "cause";
        public static final String KEY_CANCEL_MESSAGE = "cancelMessage";

        public GameModeChangeArgument(String target, @NotNull GameMode gameMode, @Nullable PlayerGameModeChangeEvent.Cause cause, @Nullable String cancelMessage)
        {
            super(target);
            this.gameMode = gameMode;
            this.cause = cause;
            this.cancelMessage = cancelMessage;
        }

        @NotNull
        GameMode gameMode;
        @Nullable
        PlayerGameModeChangeEvent.Cause cause;
        @Nullable
        String cancelMessage;

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof GameModeChangeArgument))
                return false;

            GameModeChangeArgument arg = (GameModeChangeArgument) argument;

            return super.isSame(arg) &&
                    this.gameMode == arg.gameMode &&
                    this.cause == arg.cause &&
                    Objects.equals(this.cancelMessage, arg.cancelMessage);
        }

        @Override
        public String toString()
        {
            StringBuilder builder = new StringBuilder("GameModeChangeArgument{");

            builder.append(super.toString());
            builder.append(", gameMode=").append(this.gameMode);
            if (this.cause != null)
                builder.append(", cause=").append(this.cause);
            if (this.cancelMessage != null)
                builder.append(", cancelMessage=").append(this.cancelMessage);

            return builder.append('}').toString();
        }
    }
}
