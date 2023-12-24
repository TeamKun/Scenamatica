package org.kunlab.scenamatica.action;

import lombok.Value;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

@Value
public class CompiledActionImpl implements CompiledAction
{
    ScenarioEngine engine;
    Action executor;
    InputBoard argument;
    BiConsumer<CompiledAction, Throwable> errorHandler;
    Consumer<CompiledAction> onExecute;
    ActionStructure structure;
}
