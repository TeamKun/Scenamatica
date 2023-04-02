package net.kunmc.lab.scenamatica.action.actions.chat;

import lombok.Value;
import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.action.utils.PlayerUtils;
import net.kunmc.lab.scenamatica.action.utils.TextUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.events.actor.ActorMessageReceiveEvent;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * プレイヤにメッセージを送信する/送信されることを監視するアクション。
 */
public class MessageSendAction extends AbstractAction<MessageSendAction.PlayerMessageSendActionArgument>
{
    public static final String KEY_ACTION_NAME = "message_send";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@Nullable PlayerMessageSendActionArgument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player recipient = PlayerUtils.getPlayerOrThrow(argument.getRecipient());
        String content = argument.getContent();

        recipient.sendMessage(content);
    }

    @Override
    public boolean isFired(@NotNull PlayerMessageSendActionArgument argument, @NotNull Plugin plugin, @NotNull Event event)
    {
        Player recipient = PlayerUtils.getPlayerOrThrow(argument.getRecipient());
        String content = argument.getContent();

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
    public PlayerMessageSendActionArgument deserializeArgument(@NotNull Map<String, Object> map)
    {
        MapUtils.checkType(map, PlayerMessageSendActionArgument.KEY_CONTENT, String.class);
        MapUtils.checkType(map, PlayerMessageSendActionArgument.KEY_RECIPIENT, String.class);

        return new PlayerMessageSendActionArgument(
                (String) map.get(PlayerMessageSendActionArgument.KEY_CONTENT),
                (String) map.get(PlayerMessageSendActionArgument.KEY_RECIPIENT)
        );
    }

    @Value
    public static class PlayerMessageSendActionArgument implements ActionArgument
    {
        public static final String KEY_CONTENT = "content";
        public static final String KEY_RECIPIENT = "recipient";

        @NotNull
        String content;
        @NotNull
        String recipient;

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof PlayerMessageSendActionArgument))
                return false;

            PlayerMessageSendActionArgument a = (PlayerMessageSendActionArgument) argument;
            return Objects.equals(this.content, a.content) && Objects.equals(this.recipient, a.recipient);
        }
    }
}
