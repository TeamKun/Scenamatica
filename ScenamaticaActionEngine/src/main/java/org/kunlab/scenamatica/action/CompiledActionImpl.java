package org.kunlab.scenamatica.action;

import lombok.Value;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;

import java.util.function.BiConsumer;

@Value
public class CompiledActionImpl implements CompiledAction
{
    Action executor;
    ActionContext context;
    ActionStructure structure;

    BiConsumer<CompiledAction, Throwable> errorHandler;
    BiConsumer<ActionResult, ScenarioType> onExecute;
}
