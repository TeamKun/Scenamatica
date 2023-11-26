package org.kunlab.scenamatica.action;

import lombok.Value;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Value
public class CompiledActionImpl<A extends ActionArgument> implements CompiledAction<A>
{
    ScenarioEngine engine;
    Action<A> executor;
    A argument;
    BiConsumer<CompiledAction<?>, Throwable> errorHandler;
    Consumer<CompiledAction<?>> onExecute;
    ActionStructure structure;
}
