package org.kunlab.scenamatica.action.actions.server;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.action.utils.PlayerLikeCommandSenders;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class BroadcastMessageAction extends AbstractServerAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "broadcast";
    public static final InputToken<String> IN_MESSAGE = ofInput(
            "message",
            String.class
    );
    public static final InputToken<List<PlayerSpecifier>> IN_RECIPIENTS = ofInput(
            "recipients",
            InputTypeToken.ofList(PlayerSpecifier.class),
            ofTraverser(InputTypeToken.ofList(String.class), (ser, list) -> {
                List<PlayerSpecifier> recipients = new ArrayList<>();
                for (String rawRecipient : list)
                    recipients.add(ser.tryDeserializePlayerSpecifier(rawRecipient));

                return recipients;
            })
    );
    public static final InputToken<String> IN_PERMISSION = ofInput(
            "permission",
            String.class
    );
    public static final InputToken<Boolean> IN_STRICT_RECIPIENTS = ofInput(
            "strictRecipients",
            Boolean.class
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        String message = argument.get(IN_MESSAGE);
        String permission = argument.orElse(IN_PERMISSION, () -> null);
        List<PlayerSpecifier> recipients = argument.orElse(IN_RECIPIENTS, () -> null);

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
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof BroadcastMessageEvent))
            return false;

        BroadcastMessageEvent e = (BroadcastMessageEvent) event;

        if (argument.isPresent(IN_MESSAGE))
        {
            Pattern pattern = Pattern.compile(argument.get(IN_MESSAGE));
            Matcher matcher = pattern.matcher(((TextComponent) e.message()).content());
            if (!matcher.find())
                return false;
        }

        if (argument.isPresent(IN_RECIPIENTS))
        {
            List<PlayerSpecifier> expectedRecipients = argument.get(IN_RECIPIENTS);
            Set<CommandSender> actualRecipients = e.getRecipients();

            for (PlayerSpecifier expectedRecipient : expectedRecipients)
            {
                CommandSender csExceptedRecipient = PlayerLikeCommandSenders.getCommandSenderOrNull(expectedRecipient, engine.getContext());
                if (csExceptedRecipient == null || !actualRecipients.contains(csExceptedRecipient))
                    return false;
            }

            // 存在することのチェックは終わったので, 存在しないこと(余分なプレイヤがいないこと)をチェックする
            return !argument.orElse(IN_STRICT_RECIPIENTS, () -> false) || actualRecipients.size() == expectedRecipients.size();
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
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_MESSAGE, IN_RECIPIENTS);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_MESSAGE)
                    .register(IN_PERMISSION);
        else if (type == ScenarioType.ACTION_EXPECT)
            board.register(IN_STRICT_RECIPIENTS);
        return board;
    }
}
