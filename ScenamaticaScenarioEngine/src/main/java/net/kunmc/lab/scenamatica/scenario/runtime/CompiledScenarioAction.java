package net.kunmc.lab.scenamatica.scenario.runtime;

import lombok.Value;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import net.kunmc.lab.scenamatica.scenario.ScenarioActionListener;

@Value
public class CompiledScenarioAction<A extends ActionArgument>
{
    ScenarioBean bean;
    ScenarioType type;
    Action<A> action;
    A argument;

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
