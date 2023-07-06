package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.action.utils.TextUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
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

public class PlayerQuitAction extends AbstractPlayerAction<PlayerQuitAction.Argument>
        implements Requireable<PlayerQuitAction.Argument>, Watchable<PlayerQuitAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_quit";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        PlayerQuitEvent.QuitReason reason = argument.getReason();
        Component quitMessage = argument.getQuitMessage() == null ? null: Component.text(argument.getQuitMessage());

        Player target = argument.getTarget();
        Actor targetActor = null;
        if (reason != PlayerQuitEvent.QuitReason.KICKED)
            targetActor = PlayerUtils.getActorOrThrow(engine, target);

        switch (reason)
        {
            case KICKED:
                target.kick(quitMessage);
                break;
            case DISCONNECTED:
                targetActor.leaveServer();
                break;
            case TIMED_OUT:
                targetActor.kickTimeout();
                break;
            case ERRONEOUS_STATE:
                targetActor.kickErroneous();
                break;
        }
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        if (argument.getQuitMessage() == null)
            return;


        switch (type)
        {
            case ACTION_EXECUTE:
                if (argument.getReason() == PlayerQuitEvent.QuitReason.DISCONNECTED)
                    throw new IllegalArgumentException("Quit message is not allowed when executing player quitting by disconnection.");
                break;
            case CONDITION_REQUIRE:
                throw new IllegalArgumentException("Quit message is not allowed in condition requiring scenario.");
        }

    }

    @Override
    public boolean isConditionFulfilled(@Nullable PlayerQuitAction.Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);
        return PlayerUtils.getPlayerOrNull(argument.getTargetSpecifier()) == null;
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerQuitEvent;
        PlayerQuitEvent e = (PlayerQuitEvent) event;

        Component quitMessage = e.quitMessage();
        String expectedQuitMessage = argument.getQuitMessage();
        if (!(expectedQuitMessage == null || TextUtils.isSameContent(quitMessage, expectedQuitMessage)))
            return false;

        PlayerQuitEvent.QuitReason quitReason = e.getReason();
        PlayerQuitEvent.QuitReason expectedQuitReason = argument.getReason();

        return expectedQuitReason == null || quitReason == expectedQuitReason;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerQuitEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                super.deserializeTarget(map),
                MapUtils.getOrNull(map, Argument.KEY_QUIT_MESSAGE),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_QUIT_REASON, PlayerQuitEvent.QuitReason.class)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        private static final String KEY_QUIT_MESSAGE = "message";
        private static final String KEY_QUIT_REASON = "reason"; // Paper

        private static final PlayerQuitEvent.QuitReason DEFAULT_REASON_ON_EXPECT_MODE
                = PlayerQuitEvent.QuitReason.KICKED;

        @Nullable
        String quitMessage;
        @Nullable
        PlayerQuitEvent.QuitReason reason;

        public Argument(@NotNull String targetSpecifier, @Nullable String quitMessage, @Nullable PlayerQuitEvent.QuitReason reason)
        {
            super(targetSpecifier);
            this.quitMessage = quitMessage;
            this.reason = reason;
        }

        public PlayerQuitEvent.QuitReason getReason()
        {
            return this.reason == null ? DEFAULT_REASON_ON_EXPECT_MODE: this.reason;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && Objects.equals(this.quitMessage, arg.quitMessage)
                    && this.reason == arg.reason;
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_QUIT_MESSAGE, this.quitMessage,
                    KEY_QUIT_REASON, this.reason
            );
        }
    }
}
