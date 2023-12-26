package org.kunlab.scenamatica.action.actions.server;

import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.server.TabCompleteEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabCompleteAction extends AbstractServerAction
        implements Executable, Watchable
{

    public static final String KEY_ACTION_NAME = "tab_complete";
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
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        CommandSender sender = PlayerLikeCommandSenders.getCommandSenderOrThrow(
                argument.orElse(IN_SENDER, () -> null),
                engine.getContext()
        );
        String buffer = argument.get(IN_BUFFER);
        List<String> completions = argument.orElse(IN_COMPLETIONS, ArrayList::new);
        // ↑ Collections.emptyList() はプラグインの動作に影響を与えるので使わない

        TabCompleteEvent event = new TabCompleteEvent(sender, buffer, completions);
        engine.getPlugin().getServer().getPluginManager().callEvent(event);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        assert event instanceof TabCompleteEvent;
        TabCompleteEvent e = (TabCompleteEvent) event;

        if (argument.isPresent(IN_BUFFER))
        {
            String expectedBuffer = argument.get(IN_BUFFER);
            String actualBuffer = e.getBuffer();

            Pattern pattern = Pattern.compile(expectedBuffer);
            Matcher matcher = pattern.matcher(actualBuffer);

            if (!matcher.matches())
                return false;
        }

        boolean strict = argument.orElse(IN_STRICT, () -> false);

        return checkCompletions(argument.orElse(IN_COMPLETIONS, () -> null),
                e.getCompletions(), strict
        ) && argument.ifPresent(
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
