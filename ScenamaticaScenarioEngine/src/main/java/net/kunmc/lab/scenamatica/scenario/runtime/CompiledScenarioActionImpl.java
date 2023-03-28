package net.kunmc.lab.scenamatica.scenario.runtime;

import lombok.Value;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioActionListener;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;

@Value
public class CompiledScenarioActionImpl<A extends ActionArgument> implements CompiledScenarioAction<A>
{
    ScenarioBean bean;
    ScenarioType type;
    Action<A> action;
    A argument;

    @Override
    public void execute(ActionManager manager, ScenarioActionListener listener)
    {
        manager.queueExecute(
                this.getAction(),
                this.getArgument(),
                listener::onActionError,
                listener::onActionExecuted
        );
    }
}
