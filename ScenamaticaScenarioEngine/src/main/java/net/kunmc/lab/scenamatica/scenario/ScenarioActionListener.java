package net.kunmc.lab.scenamatica.scenario;

import lombok.AllArgsConstructor;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class ScenarioActionListener
{
    private final ScenarioEngine engine;

    public <A extends ActionArgument> void onActionError(@NotNull CompiledAction<A> action, @NotNull Throwable error)
    {

    }

    public <A extends ActionArgument> void onActionExecuted(@NotNull CompiledAction<A> action)
    {

    }
}
