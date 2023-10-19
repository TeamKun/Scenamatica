package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.TextUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerJoinAction extends AbstractPlayerAction<PlayerJoinAction.Argument>
        implements Executable<PlayerJoinAction.Argument>, Watchable<PlayerJoinAction.Argument>, Requireable<PlayerJoinAction.Argument>
{
    // OfflinePlayer を扱うため, 通常の PlayerAction とは違う実装をする。

    public static final String KEY_ACTION_NAME = "player_join";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        String targetSpecifier = argument.getTargetSpecifier();  // argument.getTarget() は必ず null になる

        if (PlayerUtils.getPlayerOrNull(targetSpecifier) != null)
            throw new IllegalArgumentException("Cannot execute player join action because player is already online.");

        Actor actor = PlayerUtils.getActorByStringOrThrow(engine, targetSpecifier);

        actor.joinServer();
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        assert event instanceof PlayerJoinEvent;
        PlayerJoinEvent e = (PlayerJoinEvent) event;
        Player player = e.getPlayer();
        Component message = e.joinMessage();

        // ターゲットが存在するか。 UUID と Player#getName() で判定する。
        String targetSpecifier = argument.getTargetSpecifier();
        if (!(targetSpecifier == null || player.getName().equalsIgnoreCase(targetSpecifier)
                || this.isSameUUIDString(player.getUniqueId().toString(), targetSpecifier)))
            return false;

        String expectedJoinMessage = argument.getJoinMessage();
        return expectedJoinMessage == null || TextUtils.isSameContent(message, expectedJoinMessage);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerJoinEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                super.deserializeTarget(map),
                MapUtils.getOrNull(map, Argument.KEY_JOIN_MESSAGE)
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);
        return PlayerUtils.getPlayerOrNull(argument.getTargetSpecifier()) != null;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_JOIN_MESSAGE = "message";

        String joinMessage;

        public Argument(String target, String joinMessage)
        {
            super(target);
            this.joinMessage = joinMessage;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;
            Argument arg = (Argument) argument;

            return super.isSame(argument)
                    && Objects.equals(this.joinMessage, arg.joinMessage);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_JOIN_MESSAGE, this.joinMessage
            );
        }
    }
}
