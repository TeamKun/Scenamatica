package org.kunlab.scenamatica.action.actions.scenamatica;

import org.kunlab.scenamatica.action.AbstractAction;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractScenamaticaAction extends AbstractAction
{
    public static List<? extends AbstractScenamaticaAction> getActions()
    {
        List<AbstractScenamaticaAction> actions = new ArrayList<>();

        actions.add(new MessageAction());
        actions.add(new MilestoneAction());
        actions.add(new NegateAction());

        return actions;
    }

}
