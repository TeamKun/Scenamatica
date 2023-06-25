package org.kunlab.scenamatica.action.actions.scenamatica;

import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;

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
