package net.kunmc.lab.scenamatica.action.actions.chat;

import lombok.Value;
import net.kunmc.lab.scenamatica.action.utils.PlayerUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.events.actor.ActorMessageReceiveEvent;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

/**
 * プレイヤにメッセージを送信する/送信されることを監視するアクション。
 */
public class PlayerMessageSendAction implements Action<PlayerMessageSendAction.PlayerMessageSendActionArgument>
{
    @Override
    public void execute(@Nullable PlayerMessageSendActionArgument argument)
    {
        if (argument == null)
            throw new IllegalArgumentException("Cannot execute action without argument.");

        Player recipient = PlayerUtils.getPlayerOrThrow(argument.getRecipient());
        String content = argument.getContent();

        recipient.sendMessage(content);
    }

    @Override
    public void onStartWatching(@Nullable PlayerMessageSendActionArgument argument, @NotNull Plugin plugin, @Nullable Event event)
    {
    }

    @Override
    public boolean isFired(@NotNull PlayerMessageSendActionArgument argument, @NotNull Plugin plugin, @NotNull Event event)
    {
        Player recipient = PlayerUtils.getPlayerOrThrow(argument.getRecipient());
        String content = argument.getContent();

        assert event instanceof ActorMessageReceiveEvent;
        ActorMessageReceiveEvent e = (ActorMessageReceiveEvent) event;

        TextComponent message = e.getMessage();
        return (message.toPlainText().equals(content) || message.toLegacyText().equals(content))
                && e.getPlayer().getUniqueId().equals(recipient.getUniqueId());
    }

    @Override
    public Class<? extends Event>[] getAttachingEvents()
    {
        return new Class[]{
                ActorMessageReceiveEvent.class
        };
    }

    @Override
    public PlayerMessageSendActionArgument deserializeArgument(@NotNull Map<String, Object> map)
    {
        MapUtils.checkType(map, "content", String.class);
        MapUtils.checkType(map, "recipient", String.class);

        return new PlayerMessageSendActionArgument(
                (String) map.get("content"),
                (String) map.get("recipient")
        );
    }

    @Value
    public static class PlayerMessageSendActionArgument implements ActionArgument
    {
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
