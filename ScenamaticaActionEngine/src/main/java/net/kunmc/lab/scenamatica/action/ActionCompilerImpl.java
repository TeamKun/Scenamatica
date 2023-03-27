package net.kunmc.lab.scenamatica.action;

import net.kunmc.lab.scenamatica.enums.ActionType;
import net.kunmc.lab.scenamatica.interfaces.ScenamaticaRegistry;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.action.ActionCompiler;
import net.kunmc.lab.scenamatica.interfaces.action.CompiledAction;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.apache.logging.log4j.util.BiConsumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class ActionCompilerImpl implements ActionCompiler
{
    private static final Map<ActionType, Class<? extends Action<? extends ActionArgument>>> BY_NAME;

    static
    {
        BY_NAME = new HashMap<>();

        // TODO: アクションを登録する。
    }

    @Override
    public <A extends ActionArgument> CompiledAction<A> compile(@NotNull ScenamaticaRegistry registry,
                                                                @NotNull ActionBean bean,
                                                                @Nullable BiConsumer<CompiledAction<A>, Throwable> reportErrorTo,
                                                                @Nullable Consumer<CompiledAction<A>> onSuccess)
    {
        Class<? extends Action<A>> actionClass;
        try
        {
            // noinspection unchecked
            actionClass = (Class<? extends Action<A>>) BY_NAME.get(bean.getType());
        }
        catch (ClassCastException e)
        {
            registry.getExceptionHandler().report(e);
            throw new IllegalArgumentException("Unknown action type: " + bean.getType(), e);
        }

        if (actionClass == null)
            throw new IllegalArgumentException("Unknown action type: " + bean.getType());

        Constructor<? extends Action<A>> constructor = getActionConstructor(registry, actionClass);
        Action<A> action = createInstance(registry, constructor);

        A argument = null;
        if (bean.getArguments() != null)
            argument = action.deserializeArgument(bean.getArguments());

        return new CompiledActionImpl<>(action, argument, reportErrorTo, onSuccess);
    }

    private static <A extends ActionArgument> Constructor<? extends Action<A>> getActionConstructor(ScenamaticaRegistry registry, Class<? extends Action<A>> actionClass)
    {
        try
        {
            return actionClass.getConstructor();
        }
        catch (NoSuchMethodException e)
        {
            registry.getExceptionHandler().report(e);
            throw new IllegalStateException("Action class must have a constructor with ActionArgument as its only parameter.", e);
        }
    }

    private static <A extends ActionArgument> Action<A> createInstance(ScenamaticaRegistry registry, Constructor<? extends Action<A>> constructor)
    {
        try
        {
            return constructor.newInstance();
        }
        catch (Exception e)
        {
            registry.getExceptionHandler().report(e);
            throw new IllegalStateException("Failed to create an instance of action class.", e);
        }
    }
}
