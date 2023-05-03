package net.kunmc.lab.scenamatica.action.actions.scenamatica;

import lombok.Value;
import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.action.utils.PlayerUtils;
import net.kunmc.lab.scenamatica.action.utils.TextUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.events.actor.ActorMessageReceiveEvent;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * プレイヤにメッセージを送信する/送信されることを監視するアクション。
 */
public class MessageAction extends AbstractAction<MessageAction.Argument>
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
        Player recipient = PlayerUtils.getPlayerOrThrow(argument.getRecipient());
        String content = argument.getMessage();

        assert event instanceof ActorMessageReceiveEvent;
        ActorMessageReceiveEvent e = (ActorMessageReceiveEvent) event;

        TextComponent message = e.getMessage();
        return TextUtils.isSameContent(message, content)
                && e.getPlayer().getUniqueId().equals(recipient.getUniqueId());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                ActorMessageReceiveEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map)
    {
        MapUtils.checkType(map, Argument.KEY_MESSAGE, String.class);
        MapUtils.checkType(map, Argument.KEY_RECIPIENT, String.class);

        return new Argument(
                (String) map.get(Argument.KEY_MESSAGE),
                (String) map.get(Argument.KEY_RECIPIENT)
        );
    }

    @Value
    public static class Argument implements ActionArgument
    {
        public static final String KEY_MESSAGE = "message";
        public static final String KEY_RECIPIENT = "recipient";

        @NotNull
        String message;
        @NotNull
        String recipient;

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument a = (Argument) argument;
            return Objects.equals(this.message, a.message) && Objects.equals(this.recipient, a.recipient);
        }

        @Override
        public String getArgumentString()
        {
            return String.format("content=%s, recipient=%s", this.message, this.recipient);
        }
    }
}
