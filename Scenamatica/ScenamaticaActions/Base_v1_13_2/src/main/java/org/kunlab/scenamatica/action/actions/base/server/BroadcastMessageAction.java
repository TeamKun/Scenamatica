package org.kunlab.scenamatica.action.actions.base.server;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.server.BroadcastMessageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.action.utils.PlayerLikeCommandSenders;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Action(value = "broadcast", supportsUntil = MinecraftVersion.V1_13_2)
@ActionDoc(
        name = "メッセージのブロードキャスト",
        description = "メッセージをブロードキャストします。",
        events = {
                BroadcastMessageEvent.class
        },

        // supportsUntil = MCVersion.V1_13_2,  // <- 内部の変更なのでバージョン取らない。

        executable = "メッセージをブロードキャストします。",
        watchable = "メッセージがブロードキャストされることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = BroadcastMessageAction.KEY_OUT_MESSAGE,
                        description = "メッセージです。",
                        type = String.class
                ),
                @OutputDoc(
                        name = BroadcastMessageAction.KEY_OUT_RECIPIENTS,
                        description = "受信者です。",
                        type = PlayerSpecifier.class
                )
        }
)
public class BroadcastMessageAction extends AbstractServerAction
        implements Executable, Watchable
{
    @InputDoc(
            name = "message",
            description = "送信するメッセージ, または受信メッセージの判定用正規表現です。\n" +
                    "+ シナリオの種類が `execute` の場合は、この引数の値がそのまま送信されます。\n" +
                    "+ シナリオの種類が `except`  の場合は、この引数の値が正規表現として扱われ、マッチしているか検証されます。",
            type = String.class
    )
    public static final InputToken<String> IN_MESSAGE = ofInput(
            "message",
            String.class
    );
    @InputDoc(
            name = "recipients",
            description = "受信者です。",
            type = PlayerSpecifier.class,
            admonitions = {
                    @Admonition(
                            type = AdmonitionType.INFORMATION,
                            title = "コンソールを指定しますか？",
                            content = "コンソールを受信者として指定する場合は, プレイヤ指定子の代わりに `<CONSOLE>` を指定します。"
                    )
            }
    )
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
    @InputDoc(
            name = "permission",
            description = "ブロードキャストされたメッセージを受け取る権限です。",
            type = String.class,
            availableFor = ActionMethod.EXECUTE
    )
    public static final InputToken<String> IN_PERMISSION = ofInput(
            "permission",
            String.class
    );
    @InputDoc(
            name = "strictRecipients",
            description = "受信者が厳密に一致させる必要があるかどうかです。\n" +
                    "これを有効にすると, 余分な受信者がいないことを確認します。",
            type = boolean.class,
            availableFor = ActionMethod.WATCH
    )
    public static final InputToken<Boolean> IN_STRICT_RECIPIENTS = ofInput(
            "strictRecipients",
            Boolean.class
    );

    public static final String KEY_OUT_MESSAGE = "message";
    public static final String KEY_OUT_RECIPIENTS = "recipients";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        String message = ctxt.input(IN_MESSAGE);
        String permission = ctxt.orElseInput(IN_PERMISSION, () -> null);
        List<PlayerSpecifier> recipients = ctxt.orElseInput(IN_RECIPIENTS, () -> null);

        if (permission == null)
            if (recipients == null || recipients.isEmpty())
                Bukkit.broadcastMessage(message);
            else
                this.simulateBukkitBroadcast(ctxt, message, recipients);
        else
            Bukkit.broadcast(message, permission);
    }

    private void simulateBukkitBroadcast(@NotNull ActionContext ctxt, @NotNull String message,
                                         @NotNull List<? extends PlayerSpecifier> recipients)
    {
        Set<PlayerSpecifier> recipientsSet = new HashSet<>(recipients);
        Set<CommandSender> csRecipientsSet = recipientsSet.stream()
                .map(ps -> PlayerLikeCommandSenders.getCommandSenderOrThrow(ps, ctxt.getContext()))
                .collect(Collectors.toSet());

        BroadcastMessageEvent broadcastMessageEvent = this.createEvent(message, csRecipientsSet);
        Bukkit.getPluginManager().callEvent(broadcastMessageEvent);

        if (broadcastMessageEvent.isCancelled())
            return;

        message = broadcastMessageEvent.getMessage();

        this.makeOutputs(ctxt, message, new ArrayList<>(csRecipientsSet));
        for (CommandSender recipient : broadcastMessageEvent.getRecipients())
            recipient.sendMessage(message);
    }

    protected BroadcastMessageEvent createEvent(@NotNull String message, @NotNull Set<CommandSender> recipients)
    {
        return new BroadcastMessageEvent(message, recipients);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof BroadcastMessageEvent))
            return false;

        BroadcastMessageEvent e = (BroadcastMessageEvent) event;

        String message = e.getMessage();
        if (ctxt.hasInput(IN_MESSAGE))
        {
            Pattern pattern = Pattern.compile(ctxt.input(IN_MESSAGE));
            Matcher matcher = pattern.matcher(message);
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
            this.makeOutputs(ctxt, message, new ArrayList<>(e.getRecipients()));

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @Nullable String message, @NotNull List<CommandSender> recipients)
    {
        if (message != null)
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
