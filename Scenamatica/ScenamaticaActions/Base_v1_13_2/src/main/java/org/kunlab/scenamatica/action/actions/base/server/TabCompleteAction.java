package org.kunlab.scenamatica.action.actions.base.server;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.server.TabCompleteEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.action.utils.PlayerLikeCommandSenders;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ActionMeta("tab_complete")
public class TabCompleteAction extends AbstractServerAction
        implements Executable, Watchable
{

    public static final InputToken<PlayerSpecifier> IN_SENDER = ofInput(
            "sender",
            PlayerSpecifier.class,
            ofPlayer()
    );
    public static final InputToken<String> IN_BUFFER = ofInput(
            "buffer",
            String.class
    );
    public static final InputToken<List<String>> IN_COMPLETIONS = ofInput(
            "completions",
            InputTypeToken.ofList(String.class)
    );
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
