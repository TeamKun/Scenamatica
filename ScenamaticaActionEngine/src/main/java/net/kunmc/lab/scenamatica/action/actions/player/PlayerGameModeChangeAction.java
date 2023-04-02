package net.kunmc.lab.scenamatica.action.actions.player;

import lombok.Value;
import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.action.utils.PlayerUtils;
import net.kunmc.lab.scenamatica.action.utils.TextUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
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

public class PlayerGameModeChangeAction extends AbstractAction<PlayerGameModeChangeAction.GameModeChangeArgument>
{

    @Override
    public String getName()
    {
        //noinspection SpellCheckingInspection
        return "player_gamemode_change";
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
        MapUtils.checkType(map, "target", String.class);
        MapUtils.checkEnumName(map, "gameMode", GameMode.class);
        MapUtils.checkEnumNameIfContains(map, "cause", PlayerGameModeChangeEvent.Cause.class);
        MapUtils.checkTypeIfContains(map, "cancelMessage", String.class);

        String target = (String) map.get("target");
        GameMode gameMode = MapUtils.getAsEnum(map, "gameMode", GameMode.class);
        PlayerGameModeChangeEvent.Cause cause = MapUtils.getAsEnumOrNull(map, "cause", PlayerGameModeChangeEvent.Cause.class);
        String cancelMessage = MapUtils.getOrNull(map, "cancelMessage");

        return new GameModeChangeArgument(
                target,
                gameMode,
                cause,
                cancelMessage
        );
    }

    @Value
    public static class GameModeChangeArgument implements ActionArgument
    {
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
                    this.cancelMessage == arg.cancelMessage;
        }
    }
}
