package org.kunlab.scenamatica.action.actions.base.server;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.server.TabCompleteEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.action.utils.PlayerLikeCommandSenders;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Action("tab_complete")
@ActionDoc(
        name = "タブ補完",
        description = "タブ補完を実行します。",
        events = {
                TabCompleteEvent.class
        },

        executable = "タブ補完を実行します。",
        expectable = "タブ補完が実行されることを期待します。",
        requireable = ActionDoc.UNALLOWED
)
public class TabCompleteAction extends AbstractServerAction
        implements Executable, Expectable
{
    @InputDoc(
            name = "sender",
            description = "送信者です。",
            type = PlayerSpecifier.class,
            admonitions = {
                    @Admonition(
                            type = AdmonitionType.INFORMATION,
                            title = "コンソールを指定しますか？",
                            content = "コンソールを送信者として指定する場合は, プレイヤ指定子の代わりに `<CONSOLE>` を指定します。"
                    )
            }
    )
    public static final InputToken<PlayerSpecifier> IN_SENDER = ofInput(
            "sender",
            PlayerSpecifier.class,
            ofPlayer()
    );
    @InputDoc(
            name = "buffer",
            description = "入力途中の不完全なコマンドです。\n" +
                    "判定時には正規表現を使えます。",
            type = String.class
    )
    public static final InputToken<String> IN_BUFFER = ofInput(
            "buffer",
            String.class
    );
    @InputDoc(
            name = "completions",
            description = "補完候補です。",
            type = String[].class
    )
    public static final InputToken<List<String>> IN_COMPLETIONS = ofInput(
            "completions",
            InputTypeToken.ofList(String.class)
    );
    @InputDoc(
            name = "strict",
            description = "厳密な判定を行うかどうかです。",
            type = boolean.class,

            availableFor = ActionMethod.EXPECT
    )
    public static final InputToken<Boolean> IN_STRICT = ofInput(
            "strict",
            Boolean.class,
            false
    );

    private static boolean checkCompletions(@Nullable List<String> expected, List<String> actual, boolean strict)
    {
        if (expected == null)
            return true;

        if (strict && expected.size() != actual.size())
            return false;

        for (String expectedCompletion : expected)
        {
            Pattern pattern = Pattern.compile(expectedCompletion);
            if (actual.stream().noneMatch(pattern.asPredicate()))
                return false;
        }

        return true;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        CommandSender sender = PlayerLikeCommandSenders.getCommandSenderOrThrow(
                ctxt.orElseInput(IN_SENDER, () -> null),
                ctxt.getContext()
        );
        String buffer = ctxt.input(IN_BUFFER);
        List<String> completions = ctxt.orElseInput(IN_COMPLETIONS, ArrayList::new);
        // ↑ Collections.emptyList() はプラグインの動作に影響を与えるので使わない

        TabCompleteEvent event = new TabCompleteEvent(sender, buffer, completions);
        ctxt.getEngine().getPlugin().getServer().getPluginManager().callEvent(event);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        assert event instanceof TabCompleteEvent;
        TabCompleteEvent e = (TabCompleteEvent) event;

        if (ctxt.hasInput(IN_BUFFER))
        {
            String expectedBuffer = ctxt.input(IN_BUFFER);
            String actualBuffer = e.getBuffer();

            Pattern pattern = Pattern.compile(expectedBuffer);
            Matcher matcher = pattern.matcher(actualBuffer);

            if (!matcher.matches())
                return false;
        }

        boolean strict = ctxt.orElseInput(IN_STRICT, () -> false);

        return checkCompletions(
                ctxt.orElseInput(IN_COMPLETIONS, () -> null),
                e.getCompletions(), strict
        ) && ctxt.ifHasInput(
                IN_SENDER,
                sender -> PlayerLikeCommandSenders.isSpecifiedSender(e.getSender(), sender)
        );
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                TabCompleteEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_SENDER, IN_BUFFER, IN_COMPLETIONS, IN_STRICT);
        if (type == ScenarioType.ACTION_EXECUTE)
            board = board.requirePresent(IN_BUFFER);

        return board;
    }
}
