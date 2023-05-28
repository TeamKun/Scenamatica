package net.kunmc.lab.scenamatica.action.actions.scenamatica;

import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractScenamaticaAction<A extends ActionArgument> extends AbstractAction<A>
{
    public static List<? extends AbstractScenamaticaAction<?>> getActions()
    {
        List<AbstractScenamaticaAction<?>> actions = new ArrayList<>();

        actions.add(new MessageAction());
        actions.add(new MilestoneAction());

        return actions;
    }

}
