package net.kunmc.lab.scenamatica.action.actions.player;

import lombok.Value;
import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.action.utils.PlayerUtils;
import net.kunmc.lab.scenamatica.action.utils.TextUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
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

public class GameModeAction extends AbstractAction<GameModeAction.GameModeChangeArgument> implements Requireable<GameModeAction.GameModeChangeArgument>
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

        String target = argument.getTarget();
        GameMode gameMode = argument.getGameMode();
        // PlayerGameModeChangeEvent.Cause cause = argument.getCause();  <= EXECUTE では使用不可。
        // String cancelMessage = argument.getCancelMessage(); <= EXECUTE では使用不可。

        Player targetPlayer = PlayerUtils.getPlayerOrThrow(target);

        targetPlayer.setGameMode(gameMode);
    }

    @Override
    public boolean isFired(@NotNull GameModeChangeArgument argument, @NotNull Plugin plugin, @NotNull Event event)
    {
        if (!(event instanceof PlayerGameModeChangeEvent))
            return false;

        PlayerGameModeChangeEvent e = (PlayerGameModeChangeEvent) event;

        String target = argument.getTarget();
        GameMode gameMode = argument.getGameMode();
        PlayerGameModeChangeEvent.Cause cause = argument.getCause();
        String cancelMessage = argument.getCancelMessage();

        Player targetPlayer = PlayerUtils.getPlayerOrThrow(target);

        return e.getPlayer().equals(targetPlayer) &&
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
        MapUtils.checkType(map, GameModeChangeArgument.KEY_TARGET, String.class);
        MapUtils.checkEnumName(map, GameModeChangeArgument.KEY_GAME_MODE, GameMode.class);
        MapUtils.checkEnumNameIfContains(map, GameModeChangeArgument.KEY_CANCEL_MESSAGE, PlayerGameModeChangeEvent.Cause.class);
        MapUtils.checkTypeIfContains(map, GameModeChangeArgument.KEY_CANCEL_MESSAGE, String.class);

        String target = (String) map.get(GameModeChangeArgument.KEY_TARGET);
        GameMode gameMode = MapUtils.getAsEnum(map, GameModeChangeArgument.KEY_GAME_MODE, GameMode.class);
        PlayerGameModeChangeEvent.Cause cause = MapUtils.getAsEnum(map, GameModeChangeArgument.KEY_CAUSE, PlayerGameModeChangeEvent.Cause.class);
        String cancelMessage = (String) map.get(GameModeChangeArgument.KEY_CANCEL_MESSAGE);

        return new GameModeChangeArgument(
                target,
                gameMode,
                cause,
                cancelMessage
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable GameModeChangeArgument argument, @NotNull Plugin plugin)
    {
        argument = this.requireArgsNonNull(argument);

        String target = argument.getTarget();
        GameMode gameMode = argument.getGameMode();

        Player targetPlayer = PlayerUtils.getPlayerOrThrow(target);

        return targetPlayer.getGameMode() == gameMode;
    }

    @Override
    public void validateArgument(@Nullable GameModeChangeArgument argument)
    {
        argument = this.requireArgsNonNull(argument);

        this.throwIfPresent(GameModeChangeArgument.KEY_CAUSE, argument.getCause());
        this.throwIfPresent(GameModeChangeArgument.KEY_CANCEL_MESSAGE, argument.getCancelMessage());
    }

    @Value
    public static class GameModeChangeArgument implements ActionArgument
    {
        public static final String KEY_TARGET = "target";
        public static final String KEY_GAME_MODE = "gameMode";
        public static final String KEY_CAUSE = "cause";
        public static final String KEY_CANCEL_MESSAGE = "cancelMessage";

        @NotNull
        String target;
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

            return this.target.equals(arg.target) &&
                    this.gameMode == arg.gameMode &&
                    this.cause == arg.cause &&
                    Objects.equals(this.cancelMessage, arg.cancelMessage);
        }
    }
}
