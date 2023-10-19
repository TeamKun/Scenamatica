package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerChatEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerChatAction extends AbstractPlayerAction<PlayerChatAction.Argument>
        implements Executable<PlayerChatAction.Argument>, Watchable<PlayerChatAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_chat";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player p = argument.getTarget();
        p.chat(argument.message);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerChatEvent;
        PlayerChatEvent playerChatEvent = (PlayerChatEvent) event;

        return (argument.message == null || playerChatEvent.getMessage().matches(argument.message))
                && (argument.format == null || playerChatEvent.getFormat().matches(argument.format));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        //noinspection deprecation
        return Collections.singletonList(
                PlayerChatEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                super.deserializeTarget(map),
                (String) map.get(Argument.KEY_MESSAGE),
                (String) map.get(Argument.KEY_FORMAT)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_MESSAGE = "message";
        public static final String KEY_FORMAT = "format";

        String message;
        String format;

        public Argument(String target, String message, String format)
        {
            super(target);
            this.message = message;
            this.format = format;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && Objects.equals(this.message, arg.message)
                    && Objects.equals(this.format, arg.format);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);

            if (type == ScenarioType.ACTION_EXECUTE)
            {
                ensureNotPresent(KEY_MESSAGE, this.message);
                ensurePresent(KEY_FORMAT, this.format);
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_MESSAGE, this.message,
                    KEY_FORMAT, this.format
            );
        }
    }
}
