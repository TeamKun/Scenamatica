package org.kunlab.scenamatica.action.actions.server;

import com.destroystokyo.paper.event.server.WhitelistToggleEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.List;

public class WhitelistToggleAction extends AbstractServerAction
        implements Executable, Watchable, Requireable
{
    public static final String KEY_ACTION_NAME = "whitelist_toggle";
    public static final InputToken<Boolean> IN_ENABLED = ofInput(
            "enabled",
            Boolean.class,
            true
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Bukkit.getServer().setWhitelist(ctxt.input(IN_ENABLED));
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof WhitelistToggleEvent))
            return false;

        WhitelistToggleEvent e = (WhitelistToggleEvent) event;

        return ctxt.ifHasInput(IN_ENABLED, enabled -> enabled == e.isEnabled());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WhitelistToggleEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        return ctxt.ifHasInput(IN_ENABLED, enabled -> Bukkit.getServer().hasWhitelist() == enabled);
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(type, IN_ENABLED);
        if (type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
            board.requirePresent(IN_ENABLED);

        return board;
    }
}
