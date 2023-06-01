package net.kunmc.lab.scenamatica.action.actions.server;

import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.action.actions.server.log.ServerLogAction;
import net.kunmc.lab.scenamatica.action.actions.server.plugin.PluginDisableAction;
import net.kunmc.lab.scenamatica.action.actions.server.plugin.PluginEnableAction;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractServerAction<A extends ActionArgument> extends AbstractAction<A>
{
    public static List<? extends AbstractServerAction<?>> getActions()
    {
        List<AbstractServerAction<?>> actions = new ArrayList<>();

        actions.add(new ServerLogAction());
        actions.add(new PluginDisableAction());
        actions.add(new PluginEnableAction());
        actions.add(new BroadcastMessageAction());
        actions.add(new CommandDispatchAction());
        actions.add(new WhitelistToggleAction());

        return actions;
    }

}
