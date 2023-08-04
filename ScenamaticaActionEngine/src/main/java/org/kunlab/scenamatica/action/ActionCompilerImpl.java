package org.kunlab.scenamatica.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.actions.scenamatica.NegateAction;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.action.ActionCompiler;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionBean;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ActionCompilerImpl implements ActionCompiler
{
    public static final List<Action<? extends ActionArgument>> ACTIONS;

    static
    {
        ACTIONS = Collections.unmodifiableList(AbstractAction.getActions());
        validateActions(ACTIONS);
    }

    private static void validateActions(List<? extends Action<?>> actions)
    {
        for (Action<?> action : actions)
        {
            if (!(action.getClass().isAssignableFrom(Executable.class)
                    || action.getClass().isAssignableFrom(Watchable.class)
                    || action.getClass().isAssignableFrom(Requireable.class)))
                throw new IllegalArgumentException("Action " + action.getClass().getName() + " is not executable, watchable, or requireable, cannot be used.");
        }
    }

    private static <O extends ActionArgument> CompiledActionImpl<NegateAction.Argument<O>> processNegateAction(
            ScenarioEngine engine,
            NegateAction<O> action,
            ActionBean bean,
            BiConsumer<CompiledAction<?>, Throwable> reportErrorTo,
            Consumer<CompiledAction<?>> onSuccess)
    {
        Map<String, Object> arguments = bean.getArguments();
        if (arguments == null)
            throw new IllegalArgumentException("NegateAction requires an argument.");

        String actionName = MapUtils.getOrNull(arguments, NegateAction.Argument.KEY_ACTION);
        if (actionName == null)
            throw new IllegalArgumentException("NegateAction requires an action name.");

        Action<O> actionToBeNegated = getActionByName(actionName);
        if (actionToBeNegated == null)
            throw new IllegalArgumentException("Action " + actionName + " is not found.");
        else if (!(actionToBeNegated instanceof Requireable))
            throw new IllegalArgumentException("Action " + actionName + " is not requireable.");
        else if (actionToBeNegated instanceof NegateAction)
            throw new IllegalArgumentException("Cannot nes a negate action.");

        O argument = null;
        if (arguments.containsKey(NegateAction.Argument.KEY_ARGUMENTS))
        {

            argument = actionToBeNegated.deserializeArgument(
                    MapUtils.checkAndCastMap(
                            arguments.get(NegateAction.Argument.KEY_ARGUMENTS),
                            String.class,
                            Object.class
                    ),
                    engine.getManager().getRegistry().getScenarioFileManager().getSerializer()
            );
        }

        //noinspection unchecked
        NegateAction.Argument<O> notArgument = new NegateAction.Argument<>(
                (Requireable<O>) actionToBeNegated,
                argument
        );

        return new CompiledActionImpl<>(
                engine,
                action,
                notArgument,
                reportErrorTo,
                onSuccess,
                bean
        );
    }

    private static <A extends ActionArgument> Action<A> getActionByName(String name)
    {
        for (Action<?> a : ACTIONS)
        {
            if (a.getName().equalsIgnoreCase(name))
            {
                //noinspection unchecked
                return (Action<A>) a;
            }
        }
        return null;
    }

    @Override
    public <A extends ActionArgument> CompiledAction<A> compile(@NotNull ScenamaticaRegistry registry,
                                                                @NotNull ScenarioEngine engine,
                                                                @NotNull ActionBean bean,
                                                                @Nullable BiConsumer<CompiledAction<?>, Throwable> reportErrorTo,
                                                                @Nullable Consumer<CompiledAction<?>> onSuccess)
    {
        Action<A> action = getActionByName(bean.getType());
        if (action == null)
            throw new IllegalArgumentException("Action " + bean.getType() + " is not found.");
        else if (action instanceof NegateAction)
            //noinspection unchecked
            return (CompiledAction<A>) processNegateAction(engine, (NegateAction<A>) action, bean, reportErrorTo, onSuccess);

        BeanSerializer serializer = engine.getManager().getRegistry().getScenarioFileManager().getSerializer();

        A argument = null;
        if (bean.getArguments() != null)
            argument = action.deserializeArgument(bean.getArguments(), serializer);

        return new CompiledActionImpl<>(engine, action, argument, reportErrorTo, onSuccess, bean);
    }
}
