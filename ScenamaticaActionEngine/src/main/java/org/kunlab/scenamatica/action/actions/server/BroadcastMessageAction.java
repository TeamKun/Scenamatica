package org.kunlab.scenamatica.action.actions.server;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.action.utils.PlayerLikeCommandSenders;
import org.kunlab.scenamatica.commons.utils.TextUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
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
            }),
            ofTraverser(InputTypeToken.ofList(CommandSender.class), (ser, list) -> {
                List<PlayerSpecifier> recipients = new ArrayList<>();
                for (CommandSender rawRecipient : list)
                    if (rawRecipient instanceof Player)
                        recipients.add(ser.tryDeserializePlayerSpecifier(rawRecipient));
                    else
                        recipients.add(ser.tryDeserializePlayerSpecifier("<CONSOLE>"));

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

    public static final String KEY_OUT_MESSAGE = "message";
    public static final String KEY_OUT_RECIPIENTS = "recipients";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        String message = ctxt.input(IN_MESSAGE);
        String permission = ctxt.orElseInput(IN_PERMISSION, () -> null);
        List<PlayerSpecifier> recipients = ctxt.orElseInput(IN_RECIPIENTS, () -> null);

        if (permission == null)
            if (recipients == null || recipients.isEmpty())
                Bukkit.broadcast(Component.text(message));
            else
                this.simulateBukkitBroadcast(ctxt, Component.text(message), recipients);
        else
            Bukkit.broadcast(Component.text(message), permission);
    }

    private void simulateBukkitBroadcast(@NotNull ActionContext ctxt, @NotNull Component messageComponent,
                                         @NotNull List<? extends PlayerSpecifier> recipients)
    {
        Set<PlayerSpecifier> recipientsSet = new HashSet<>(recipients);
        Set<CommandSender> csRecipientsSet = recipientsSet.stream()
                .map(ps -> PlayerLikeCommandSenders.getCommandSenderOrThrow(ps, ctxt.getContext()))
                .collect(Collectors.toSet());

        BroadcastMessageEvent broadcastMessageEvent =
                new BroadcastMessageEvent(!Bukkit.isPrimaryThread(), messageComponent, csRecipientsSet);
        Bukkit.getPluginManager().callEvent(broadcastMessageEvent);

        if (broadcastMessageEvent.isCancelled())
            return;

        messageComponent = broadcastMessageEvent.message();

        this.makeOutputs(ctxt, TextUtils.toString(messageComponent), new ArrayList<>(csRecipientsSet));
        for (CommandSender recipient : broadcastMessageEvent.getRecipients())
            recipient.sendMessage(messageComponent);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof BroadcastMessageEvent))
            return false;

        BroadcastMessageEvent e = (BroadcastMessageEvent) event;

        if (ctxt.hasInput(IN_MESSAGE))
        {
            Pattern pattern = Pattern.compile(ctxt.input(IN_MESSAGE));
            Matcher matcher = pattern.matcher(TextUtils.toString(e.message()));
            if (!matcher.find())
                return false;
        }

        boolean result = true;
        if (ctxt.hasInput(IN_RECIPIENTS))
        {
            List<PlayerSpecifier> expectedRecipients = ctxt.input(IN_RECIPIENTS);
            Set<CommandSender> actualRecipients = e.getRecipients();

            for (PlayerSpecifier expectedRecipient : expectedRecipients)
            {
                CommandSender csExceptedRecipient = PlayerLikeCommandSenders.getCommandSenderOrNull(expectedRecipient, ctxt.getContext());
                if (csExceptedRecipient == null || !actualRecipients.contains(csExceptedRecipient))
                    return false;
            }

            // 存在することのチェックは終わったので, 存在しないこと(余分なプレイヤがいないこと)をチェックする
            result = !ctxt.orElseInput(IN_STRICT_RECIPIENTS, () -> false) || actualRecipients.size() == expectedRecipients.size();
        }

        if (result)
            this.makeOutputs(ctxt, TextUtils.toString(e.message()), new ArrayList<>(e.getRecipients()));

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull String message, @NotNull List<CommandSender> recipients)
    {
        ctxt.output(KEY_OUT_MESSAGE, message);
        ctxt.output(KEY_OUT_RECIPIENTS, recipients);
        ctxt.commitOutput();
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
