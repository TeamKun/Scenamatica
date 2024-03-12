package org.kunlab.scenamatica.action.actions.server;

import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.actions.server.log.ServerLogAction;
import org.kunlab.scenamatica.action.actions.server.plugin.PluginDisableAction;
import org.kunlab.scenamatica.action.actions.server.plugin.PluginEnableAction;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractServerAction extends AbstractAction
{
    public static List<? extends AbstractServerAction> getActions()
    {
        List<AbstractServerAction> actions = new ArrayList<>();

        actions.add(new ServerLogAction());
        actions.add(new PluginDisableAction());
        actions.add(new PluginEnableAction());
        actions.add(new BroadcastMessageAction());
        actions.add(new CommandDispatchAction());
        actions.add(new TabCompleteAction());
        actions.add(new WhitelistToggleAction());

        return actions;
    }

}
