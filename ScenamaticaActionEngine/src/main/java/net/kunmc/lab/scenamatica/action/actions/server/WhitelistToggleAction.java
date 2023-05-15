package net.kunmc.lab.scenamatica.action.actions.server;

import com.destroystokyo.paper.event.server.*;
import lombok.*;
import net.kunmc.lab.scenamatica.action.actions.*;
import net.kunmc.lab.scenamatica.commons.utils.*;
import net.kunmc.lab.scenamatica.interfaces.action.*;
import net.kunmc.lab.scenamatica.interfaces.scenario.*;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.*;
import org.bukkit.*;
import org.bukkit.event.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class WhitelistToggleAction extends AbstractAction<WhitelistToggleAction.Argument> implements Requireable<WhitelistToggleAction.Argument>
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
    public Argument deserializeArgument(@NotNull Map<String, Object> map)
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
    public static class Argument implements ActionArgument
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
            return this.enabled ? "enabled": "disabled";
        }
    }
}
