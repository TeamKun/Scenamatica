package net.kunmc.lab.scenamatica.action;

import net.kunmc.lab.scenamatica.action.actions.AbstractAction;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionCompiler;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.BeanSerializer;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ActionCompilerImpl implements ActionCompiler
{
    public static final List<Action<?>> ACTIONS;

    static
    {
        // 全部の Action を登録する
        ACTIONS = Collections.unmodifiableList(AbstractAction.getActions());
    }

    @Override
    public <A extends ActionArgument> CompiledAction<A> compile(@NotNull ScenamaticaRegistry registry,
                                                                @NotNull ScenarioEngine engine,
                                                                @NotNull ActionBean bean,
                                                                @Nullable BiConsumer<CompiledAction<A>, Throwable> reportErrorTo,
                                                                @Nullable Consumer<CompiledAction<A>> onSuccess)
    {
        Action<A> action = null;
        for (Action<?> a : ACTIONS)
        {
            if (a.getName().equals(bean.getType()))
            {
                //noinspection unchecked
                action = (Action<A>) a;
                break;
            }
        }

        if (action == null)
            throw new IllegalArgumentException("Action " + bean.getType() + " is not found.");

        BeanSerializer serializer = engine.getManager().getRegistry().getScenarioFileManager().getSerializer();

        A argument = null;
        if (bean.getArguments() != null)
            argument = action.deserializeArgument(bean.getArguments(), serializer);

        return new CompiledActionImpl<>(engine, action, argument, reportErrorTo, onSuccess, bean);
    }
}
