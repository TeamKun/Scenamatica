package org.kunlab.scenamatica.action.actions.scenamatica;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.action.utils.PlayerUtils;
import org.kunlab.scenamatica.commons.utils.TextUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.events.actor.ActorMessageReceiveEvent;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * プレイヤにメッセージを送信する/送信されることを監視するアクション。
 */
public class MessageAction extends AbstractScenamaticaAction<MessageAction.Argument>
        implements Executable<MessageAction.Argument>, Watchable<MessageAction.Argument>
{
    public static final String KEY_ACTION_NAME = "message";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable MessageAction.Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player recipient = PlayerUtils.getPlayerOrThrow(argument.getRecipient());
        String content = argument.getMessage();

        recipient.sendMessage(content);
    }

    @Override
    public boolean isFired(@NotNull MessageAction.Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        String content = argument.getMessage();
        Player recipient = null;
        if (argument.getRecipient() != null)
            recipient = PlayerUtils.getPlayerOrThrow(argument.getRecipient());

        assert event instanceof ActorMessageReceiveEvent;
        ActorMessageReceiveEvent e = (ActorMessageReceiveEvent) event;

        TextComponent message = e.getMessage();
        return (content == null || TextUtils.isSameContent(message, content))
                && (recipient == null || e.getPlayer().getUniqueId().equals(recipient.getUniqueId()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                ActorMessageReceiveEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                (String) map.get(Argument.KEY_MESSAGE),
                (String) map.get(Argument.KEY_RECIPIENT)
        );
    }

    @EqualsAndHashCode(callSuper = true)
    @Value
    public static class Argument extends AbstractActionArgument
    {
        public static final String KEY_MESSAGE = "message";
        public static final String KEY_RECIPIENT = "recipient";

        String message;
        String recipient;

        public Argument(String message, String recipient)
        {
            this.message = message;
            this.recipient = recipient;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument a = (Argument) argument;
            return Objects.equals(this.message, a.message) && Objects.equals(this.recipient, a.recipient);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                ensureNotPresent(KEY_MESSAGE, this.message);
                ensureNotPresent(KEY_RECIPIENT, this.recipient);
            }
        }

        @Override
        public String getArgumentString()
        {
            return buildArgumentString(
                    KEY_MESSAGE, this.message,
                    KEY_RECIPIENT, this.recipient
            );
        }
    }
}
