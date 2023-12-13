package org.kunlab.scenamatica.action.actions.server;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.server.TabCompleteEvent;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TabCompleteAction extends AbstractServerAction<TabCompleteAction.Argument>
        implements Executable<TabCompleteAction.Argument>, Watchable<TabCompleteAction.Argument>
{

    public static final String KEY_ACTION_NAME = "tab_complete";

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
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        CommandSender sender = PlayerLikeCommandSenders.resolveSenderOrConsoleOrThrow(argument.getSender(), engine.getContext());
        String buffer = argument.getBuffer();
        List<String> completions = argument.getCompletions();
        if (completions == null)
            completions = new ArrayList<>(); // Collections.emptyList() だと不変なのでプラグインのイベントハンドラがバグるかも。

        TabCompleteEvent event = new TabCompleteEvent(sender, buffer, completions);
        engine.getPlugin().getServer().getPluginManager().callEvent(event);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        assert event instanceof TabCompleteEvent;
        TabCompleteEvent e = (TabCompleteEvent) event;

        if (argument.getBuffer() != null)
        {
            String expectedBuffer = argument.getBuffer();
            String actualBuffer = e.getBuffer();

            Pattern pattern = Pattern.compile(expectedBuffer);
            Matcher matcher = pattern.matcher(actualBuffer);

            if (!matcher.matches())
                return false;
        }


        return checkCompletions(argument.getCompletions(), e.getCompletions(), argument.isStrict())
                && PlayerLikeCommandSenders.isSpecifiedSender(e.getSender(), argument.getSender());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                TabCompleteEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new Argument(
                PlayerSpecifierImpl.tryDeserializePlayer(map.get(Argument.KEY_SENDER), serializer),
                (String) map.get(Argument.KEY_BUFFER),
                MapUtils.getAsListOrNull(map, Argument.KEY_COMPLETIONS),
                MapUtils.getOrDefault(map, Argument.KEY_STRICT, true)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractActionArgument
    {
        public static final String KEY_SENDER = "sender";
        public static final String KEY_BUFFER = "buffer";
        public static final String KEY_COMPLETIONS = "completions";
        public static final String KEY_STRICT = "strict";

        PlayerSpecifier sender;
        String buffer;
        List<String> completions;
        boolean strict;

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return Objects.equals(this.sender, arg.sender)
                    && Objects.equals(this.buffer, arg.buffer)
                    && MapUtils.equals(this.completions, arg.completions
            );
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
                ensurePresent(KEY_BUFFER, this.buffer);
        }

        @Override
        public String getArgumentString()
        {
            return buildArgumentString(
                    KEY_SENDER, this.sender,
                    KEY_BUFFER, this.buffer,
                    KEY_COMPLETIONS, this.completions,
                    KEY_STRICT, this.strict
            );
        }
    }
}
