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
import org.kunlab.scenamatica.action.utils.PlayerLikeCommandSenders;
import org.kunlab.scenamatica.commons.specifiers.PlayerSpecifierImpl;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BroadcastMessageAction extends AbstractServerAction<BroadcastMessageAction.Argument>
        implements Executable<BroadcastMessageAction.Argument>, Watchable<BroadcastMessageAction.Argument>
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
        List<PlayerSpecifier> recipients = argument.getRecipients();

        if (permission == null)
            if (recipients == null || recipients.isEmpty())
                Bukkit.broadcast(Component.text(message));
            else
                this.simulateBukkitBroadcast(engine, Component.text(message), recipients);
        else
            Bukkit.broadcast(Component.text(message), permission);
    }

    private void simulateBukkitBroadcast(@NotNull ScenarioEngine engine, @NotNull Component messageComponent,
                                         @NotNull List<? extends PlayerSpecifier> recipients)
    {
        Set<PlayerSpecifier> recipientsSet = new HashSet<>(recipients);
        Set<CommandSender> csRecipientsSet = recipientsSet.stream()
                .map(ps -> PlayerLikeCommandSenders.getCommandSenderOrThrow(ps, engine.getContext()))
                .collect(Collectors.toSet());

        BroadcastMessageEvent broadcastMessageEvent =
                new BroadcastMessageEvent(!Bukkit.isPrimaryThread(), messageComponent, csRecipientsSet);
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

        if (argument.getMessage() != null)
        {
            Pattern pattern = Pattern.compile(argument.getMessage());
            Matcher matcher = pattern.matcher(((TextComponent) e.message()).content());
            if (!matcher.find())
                return false;
        }

        if (argument.getRecipients() != null)
        {
            List<PlayerSpecifier> expectedRecipients = argument.getRecipients();
            Set<CommandSender> actualRecipients = e.getRecipients();

            for (PlayerSpecifier expectedRecipient : expectedRecipients)
            {
                CommandSender actualRecipient = PlayerLikeCommandSenders.getCommandSenderOrNull(expectedRecipient, engine.getContext());
                if (actualRecipient == null || !actualRecipients.contains(actualRecipient))
                    return false;
            }

            // 存在することのチェックは終わったので, 存在しないこと(余分なプレイヤがいないこと)をチェックする
            return !argument.getStrictRecipients() || expectedRecipients.size() == actualRecipients.size();
        }

        return true;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                BroadcastMessageEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        List<PlayerSpecifier> recipients = new ArrayList<>();
        if (map.containsKey(Argument.KEY_RECIPIENTS))
        {
            List<?> rawRecipients = (List<?>) map.get(Argument.KEY_RECIPIENTS);
            for (Object rawRecipient : rawRecipients)
                recipients.add(PlayerSpecifierImpl.tryDeserializePlayer(rawRecipient, serializer));
        }


        return new Argument(
                (String) map.get(Argument.KEY_MESSAGE),
                recipients,
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

        String message;
        List<PlayerSpecifier> recipients;
        String permission;
        Boolean strictRecipients;

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return Objects.equals(this.message, arg.message)
                    && (arg.recipients == null || this.recipients != null && MapUtils.equals(this.recipients, arg.recipients))
                    && (this.permission == null || this.permission.equals(arg.permission))
                    && this.strictRecipients == arg.strictRecipients;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            switch (type)
            {
                case ACTION_EXPECT:
                    ensureNotPresent(KEY_PERMISSION, this.permission);
                    break;
                case ACTION_EXECUTE:
                    ensurePresent(KEY_MESSAGE, this.message);
                    break;
            }
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

        public List<CommandSender> getRecipients(ScenarioEngine engine)
        {
            return this.recipients.stream()
                    .map(ps -> PlayerLikeCommandSenders.getCommandSenderOrThrow(ps, engine.getContext()))
                    .collect(Collectors.toList());
        }
    }
}
