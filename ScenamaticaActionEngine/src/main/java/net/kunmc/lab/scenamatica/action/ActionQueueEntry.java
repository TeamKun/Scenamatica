package net.kunmc.lab.scenamatica.action;

import lombok.Value;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;

@Value
public class ActionQueueEntry<A extends ActionArgument>
{
    Action<A> action;
    A argument;

    /* non-public */ void execute()
    {
        this.action.execute(this.argument);
    }
}
