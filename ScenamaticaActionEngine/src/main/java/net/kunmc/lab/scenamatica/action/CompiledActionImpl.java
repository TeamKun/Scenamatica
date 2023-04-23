package net.kunmc.lab.scenamatica.action;

import lombok.Value;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Value
public class CompiledActionImpl<A extends ActionArgument> implements CompiledAction<A>
{
    ScenarioEngine engine;
    Action<A> action;
    A argument;
    BiConsumer<CompiledAction<A>, Throwable> errorHandler;
    Consumer<CompiledAction<A>> onExecute;
    ActionBean bean;

    @Override
    public void execute()
    {
        try
        {
            this.action.execute(this.engine, this.argument);
            this.onExecute.accept(this);
        }
        catch (Throwable e)
        {
            this.errorHandler.accept(this, e);
        }
    }
}
