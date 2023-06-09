package org.kunlab.scenamatica.action.actions.server;

import com.destroystokyo.paper.event.server.WhitelistToggleEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WhitelistToggleAction extends AbstractServerAction<WhitelistToggleAction.Argument>
        implements Requireable<WhitelistToggleAction.Argument>, Watchable<WhitelistToggleAction.Argument>
{
    public static final String KEY_ACTION_NAME = "whitelist_toggle";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);
        boolean enabled = argument.enabled;

        Bukkit.getServer().setWhitelist(enabled);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof WhitelistToggleEvent))
            return false;

        WhitelistToggleEvent e = (WhitelistToggleEvent) event;

        return e.isEnabled() == argument.enabled;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WhitelistToggleEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        MapUtils.checkContainsKey(map, Argument.KEY_ENABLED);

        return new Argument(
                (boolean) map.get(Argument.KEY_ENABLED)
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);
        return Bukkit.getServer().hasWhitelist() == argument.enabled;
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractActionArgument
    {
        public static final String KEY_ENABLED = "enabled";

        boolean enabled;

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return this.enabled == arg.enabled;
        }

        @Override
        public String getArgumentString()
        {
            return buildArgumentString(
                    KEY_ENABLED, this.enabled
            );
        }
    }
}
