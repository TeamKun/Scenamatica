package net.kunmc.lab.scenamatica.action;

import lombok.Value;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionQueueEntry;
import org.apache.logging.log4j.util.BiConsumer;

import java.util.function.Consumer;

@Value
public class ActionQueueEntryImpl<A extends ActionArgument> implements ActionQueueEntry<A>
{
    Action<A> action;
    A argument;
    BiConsumer<ActionQueueEntry<A>, Throwable> errorHandler;
    Consumer<ActionQueueEntry<A>> onExecute;

    @Override
    public void execute()
    {
        try
        {
            this.action.execute(this.argument);
            this.onExecute.accept(this);
        }
        catch (Throwable e)
        {
            this.errorHandler.accept(this, e);
        }
    }
}
