package net.kunmc.lab.scenamatica.interfaces.scenario.runtime;

import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioActionListener;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;

public interface CompiledScenarioAction<A extends ActionArgument>
{
    void execute(ActionManager manager, ScenarioActionListener listener);

    ScenarioBean getBean();

    ScenarioType getType();

    Action<A> getAction();

    A getArgument();
}
