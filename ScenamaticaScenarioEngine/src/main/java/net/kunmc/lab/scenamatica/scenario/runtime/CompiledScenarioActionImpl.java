package net.kunmc.lab.scenamatica.scenario.runtime;

import lombok.Value;
import net.kunmc.lab.scenamatica.enums.ScenarioType;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioActionListener;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenario.runtime.CompiledScenarioAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value
public class CompiledScenarioActionImpl<A extends ActionArgument> implements CompiledScenarioAction<A>
{
    @NotNull
    ScenarioBean bean;
    @NotNull
    ScenarioType type;
    @NotNull
    Action<A> action;
    @Nullable
    A argument;
    @Nullable
    CompiledScenarioAction<?> runIf;

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull ActionManager manager, @NotNull ScenarioActionListener listener)
    {
        manager.queueExecute(
                engine,
                this.getAction(),
                this.getArgument(),
                listener::onActionError,
                listener::onActionExecuted
        );
    }
}
