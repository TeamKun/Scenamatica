package org.kunlab.scenamatica.action.actions.player;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.TextUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerQuitAction extends AbstractPlayerAction<PlayerQuitAction.Argument>
        implements Executable<PlayerQuitAction.Argument>, Watchable<PlayerQuitAction.Argument>, Requireable<PlayerQuitAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_quit";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull PlayerQuitAction.Argument argument)
    {
        PlayerQuitEvent.QuitReason reason = argument.getReason(ScenarioType.ACTION_EXECUTE);
        Component quitMessage = argument.getQuitMessage() == null ? null: Component.text(argument.getQuitMessage());

        Player target = argument.getTarget(engine);
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
    public boolean isConditionFulfilled(@NotNull PlayerQuitAction.Argument argument, @NotNull ScenarioEngine engine)
    {
        Player target = argument.getTargetOrNull(engine);
        return target == null || !target.isOnline();
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
        PlayerQuitEvent.QuitReason expectedQuitReason = argument.getReason(ScenarioType.ACTION_EXPECT);

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
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new Argument(
                super.deserializeTarget(map, serializer),
                MapUtils.getOrNull(map, Argument.KEY_QUIT_MESSAGE),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_QUIT_REASON, PlayerQuitEvent.QuitReason.class)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_QUIT_MESSAGE = "message";
        public static final String KEY_QUIT_REASON = "reason"; // Paper

        private static final PlayerQuitEvent.QuitReason DEFAULT_REASON_ON_EXECUTE_MODE = PlayerQuitEvent.QuitReason.KICKED;

        String quitMessage;
        @Getter(AccessLevel.NONE)
        PlayerQuitEvent.QuitReason reason;

        public Argument(@NotNull PlayerSpecifier targetSpecifier, String quitMessage, PlayerQuitEvent.QuitReason reason)
        {
            super(targetSpecifier);
            this.quitMessage = quitMessage;
            this.reason = reason;
        }

        public PlayerQuitEvent.QuitReason getReason(ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE && this.reason == null)
                return DEFAULT_REASON_ON_EXECUTE_MODE;
            return this.reason;
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
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (this.quitMessage == null)
                return;

            if (type == ScenarioType.ACTION_EXECUTE && this.reason == PlayerQuitEvent.QuitReason.DISCONNECTED)
                throw new IllegalArgumentException("Quit message is not allowed when executing player quitting by disconnection.");
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
