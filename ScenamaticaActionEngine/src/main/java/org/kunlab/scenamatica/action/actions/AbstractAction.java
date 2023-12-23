package org.kunlab.scenamatica.action.actions;

import org.kunlab.scenamatica.action.actions.block.AbstractBlockAction;
import org.kunlab.scenamatica.action.actions.entity.AbstractEntityAction;
import org.kunlab.scenamatica.action.actions.inventory.AbstractInventoryAction;
import org.kunlab.scenamatica.action.actions.player.AbstractPlayerAction;
import org.kunlab.scenamatica.action.actions.scenamatica.AbstractScenamaticaAction;
import org.kunlab.scenamatica.action.actions.server.AbstractServerAction;
import org.kunlab.scenamatica.action.actions.world.AbstractWorldAction;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractAction<A extends ActionArgument> implements Action<A>
{
    public static List<? extends AbstractAction<?>> getActions()
    {
        List<AbstractAction<?>> actions = new ArrayList<>();

        actions.addAll(AbstractBlockAction.getActions());
        actions.addAll(AbstractEntityAction.getActions());
        actions.addAll(AbstractInventoryAction.getActions());
        actions.addAll(AbstractPlayerAction.getActions());
        actions.addAll(AbstractServerAction.getActions());
        actions.addAll(AbstractWorldAction.getActions());
        actions.addAll(AbstractScenamaticaAction.getActions());

        return actions;
    }
}
