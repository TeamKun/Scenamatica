package net.kunmc.lab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.scenamatica.action.utils.TextUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.BeanSerializer;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerKickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerKickAction extends AbstractPlayerAction<PlayerKickAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_kick";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        // leaveMessage はつかえない。

        PlayerKickEvent.Cause cause = argument.getCause();
        Component kickMessage = argument.getKickMessage() == null ? null: Component.text(argument.getKickMessage());

        Player target = argument.getTarget();
        if (cause == null)
            target.kick(kickMessage);
        else
            target.kick(kickMessage, cause);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.isFired(argument, engine, event))
            return false;

        PlayerKickEvent e = (PlayerKickEvent) event;
        Component leaveMessage = e.leaveMessage();
        Component kickMessage = e.reason();
        PlayerKickEvent.Cause cause = e.getCause();

        String expectedLeaveMessage = argument.getLeaveMessage();
        String expectedKickMessage = argument.getKickMessage();
        PlayerKickEvent.Cause expectedCause = argument.getCause();

        return (expectedLeaveMessage == null || TextUtils.isSameContent(leaveMessage, expectedLeaveMessage))
                && (expectedKickMessage == null || TextUtils.isSameContent(kickMessage, expectedKickMessage))
                && (expectedCause == null || cause == expectedCause);
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        if (type == ScenarioType.ACTION_EXECUTE && argument.getLeaveMessage() != null)
            throw new IllegalArgumentException("Leave message cannot be used in execute scenario.");
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerKickEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                super.deserializeTarget(map),
                MapUtils.getOrNull(map, Argument.KEY_LEAVE_MESSAGE),
                MapUtils.getOrNull(map, Argument.KEY_KICK_MESSAGE),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_CAUSE, PlayerKickEvent.Cause.class)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        private static final String KEY_LEAVE_MESSAGE = "leave_message";
        private static final String KEY_KICK_MESSAGE = "message";
        private static final String KEY_CAUSE = "cause";

        @Nullable
        String leaveMessage;
        @Nullable
        String kickMessage;
        @Nullable
        PlayerKickEvent.Cause cause;

        public Argument(@NotNull String target, @Nullable String leaveMessage, @Nullable String kickMessage, @Nullable PlayerKickEvent.Cause cause)
        {
            super(target);
            this.leaveMessage = leaveMessage;
            this.kickMessage = kickMessage;
            this.cause = cause;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && Objects.equals(this.leaveMessage, arg.leaveMessage)
                    && Objects.equals(this.kickMessage, arg.kickMessage)
                    && this.cause == arg.cause;
        }

        @Override
        public String getArgumentString()
        {
            StringBuilder builder = new StringBuilder(super.getArgumentString());
            if (this.leaveMessage != null)
                builder.append(", ").append(KEY_LEAVE_MESSAGE).append("=\"").append(this.leaveMessage).append("\"");
            if (this.kickMessage != null)
                builder.append(", ").append(KEY_KICK_MESSAGE).append("=\"").append(this.kickMessage).append("\"");
            if (this.cause != null)
                builder.append(", ").append(KEY_CAUSE).append("=").append(this.cause.name());

            return builder.toString();


        }
    }
}
