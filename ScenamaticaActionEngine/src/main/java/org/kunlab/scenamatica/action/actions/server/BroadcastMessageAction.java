package org.kunlab.scenamatica.action.actions.server;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BroadcastMessageAction extends AbstractServerAction<BroadcastMessageAction.Argument>
        implements Watchable<BroadcastMessageAction.Argument>
{
    public static final String KEY_ACTION_NAME = "broadcast";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        String message = argument.getMessage();
        String permission = argument.getPermission();
        List<CommandSender> recipients = argument.getRecipients();

        if (permission == null)
            if (recipients.isEmpty())
                Bukkit.broadcast(Component.text(message));
            else
                this.simulateBukkitBroadcast(Component.text(message), recipients);
        else
            Bukkit.broadcast(Component.text(message), permission);
    }

    private void simulateBukkitBroadcast(@NotNull Component messageComponent, @NotNull List<? extends CommandSender> recipients)
    {
        Set<CommandSender> recipientsSet = new HashSet<>(recipients);

        BroadcastMessageEvent broadcastMessageEvent =
                new BroadcastMessageEvent(!Bukkit.isPrimaryThread(), messageComponent, recipientsSet);
        Bukkit.getPluginManager().callEvent(broadcastMessageEvent);

        if (broadcastMessageEvent.isCancelled())
            return;

        messageComponent = broadcastMessageEvent.message();

        for (CommandSender recipient : broadcastMessageEvent.getRecipients())
            recipient.sendMessage(messageComponent);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        argument = this.requireArgsNonNull(argument);
        if (!(event instanceof BroadcastMessageEvent))
            return false;

        BroadcastMessageEvent e = (BroadcastMessageEvent) event;

        Pattern pattern = Pattern.compile(argument.getMessage());
        Matcher matcher = pattern.matcher(((TextComponent) e.message()).content());
        if (!matcher.find())
            return false;

        List<CommandSender> recipients = argument.getRecipients();
        Set<CommandSender> actualRecipients = e.getRecipients();

        for (CommandSender recipient : recipients)
            if (!actualRecipients.contains(recipient))
                return false;

        // 存在することのチェックは終わったので, 存在しないこと(余分なプレイヤがいないこと)をチェックする
        return !argument.isStrictRecipients() || recipients.size() == actualRecipients.size();
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);
        if (type == ScenarioType.ACTION_EXPECT && argument.getPermission() != null)
            throw new IllegalArgumentException("Permission is not supported in expect scenario.");

    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                BroadcastMessageEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                (String) map.get(Argument.KEY_MESSAGE),
                MapUtils.getAsListOrEmpty(map, Argument.KEY_RECIPIENTS),
                MapUtils.getOrNull(map, Argument.KEY_PERMISSION),
                MapUtils.getOrDefault(map, Argument.KEY_STRICT_RECIPIENTS, false)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractActionArgument
    {
        public static final String KEY_MESSAGE = "message";
        public static final String KEY_RECIPIENTS = "recipients";
        public static final String KEY_PERMISSION = "permission";
        public static final String KEY_STRICT_RECIPIENTS = "strictRecipients";

        private static final String CONSOLE_IDENTIFIER = "<CONSOLE>";

        @NotNull
        String message;
        @NotNull
        List<String> recipients;  // Console = <CONSOLE>
        @Nullable
        String permission;
        boolean strictRecipients;

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return arg.message.equals(this.message)
                    && arg.recipients.stream().parallel()
                    .allMatch(recipient -> this.recipients.stream().parallel()
                            .anyMatch(recipient::equals)
                    )
                    && (this.permission == null || this.permission.equals(arg.permission))
                    && this.strictRecipients == arg.strictRecipients;
        }

        @Override
        public String getArgumentString()
        {
            return buildArgumentString(
                    KEY_MESSAGE, this.message,
                    KEY_RECIPIENTS, this.recipients,
                    KEY_PERMISSION, this.permission,
                    KEY_STRICT_RECIPIENTS, this.strictRecipients
            );
        }

        public List<CommandSender> getRecipients()
        {
            List<CommandSender> result = new ArrayList<>();
            for (String recipient : this.recipients)
            {
                if (recipient.equals(CONSOLE_IDENTIFIER))
                    result.add(Bukkit.getConsoleSender());
                else
                    result.add(Bukkit.getPlayer(recipient));
            }

            return result;
        }
    }
}
