package org.kunlab.scenamatica.action;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.action.actions.scenamatica.NegateAction;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionCompiler;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionStructure;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class ActionCompilerImpl implements ActionCompiler
{
    public static final List<Action> ACTIONS;

    static
    {
        ACTIONS = Collections.unmodifiableList(AbstractAction.getActions());
        validateActions(ACTIONS);
    }

    private static void validateActions(List<? extends Action> actions)
    {
        for (Action action : actions)
        {
            if (!(action instanceof Executable) && !(action instanceof Watchable) && !(action instanceof Requireable))
                throw new IllegalArgumentException("Action " + action.getClass().getName() + " is not executable, watchable, or requireable, cannot be used.");
        }
    }

    private static CompiledActionImpl processNegateAction(
            ScenarioEngine engine,
            StructureSerializer serializer,
            NegateAction action,
            ActionStructure structure,
            BiConsumer<CompiledAction, Throwable> reportErrorTo,
            BiConsumer<ActionResult, ScenarioType> onSuccess)
    {
        Map<String, Object> arguments = structure.getArguments();
        if (arguments == null)
            throw new IllegalArgumentException("NegateAction requires an argument.");

        String actionName = MapUtils.getOrNull(arguments, NegateAction.KEY_IN_ACTION);
        if (actionName == null)
            throw new IllegalArgumentException("NegateAction requires an action name.");

        Action actionToBeNegated = getActionByName(actionName);
        if (actionToBeNegated == null)
            throw new IllegalArgumentException("Action " + actionName + " is not found.");
        else if (!(actionToBeNegated instanceof Requireable))
            throw new IllegalArgumentException("Action " + actionName + " is not requireable.");
        else if (actionToBeNegated instanceof NegateAction)
            throw new IllegalArgumentException("Cannot nes a negate action.");

        InputBoard argument = actionToBeNegated.getInputBoard(ScenarioType.CONDITION_REQUIRE);
        if (arguments.containsKey(NegateAction.KEY_IN_ARGUMENTS))
            argument.compile(serializer, MapUtils.checkAndCastMap(arguments.get(NegateAction.KEY_IN_ARGUMENTS)));

        InputBoard negateArgument = action.getInputBoard(ScenarioType.CONDITION_REQUIRE);
        negateArgument.compile(serializer, new HashMap<String, Object>()
        {{
            this.put(NegateAction.KEY_IN_ACTION, actionToBeNegated);
            this.put(NegateAction.KEY_IN_ARGUMENTS, new ActionContextImpl(engine, argument, engine.getPlugin().getLogger()));
        }});

        return new CompiledActionImpl(
                action,
                new ActionContextImpl(engine, negateArgument, engine.getPlugin().getLogger()),
                structure,
                reportErrorTo,
                onSuccess
        );
    }

    private static Action getActionByName(String name)
    {
        for (Action a : ACTIONS)
            if (a.getName().equalsIgnoreCase(name))
                return a;

        return null;
    }

    @Override
    public CompiledAction compile(@NotNull ScenarioEngine engine,
                                  @NotNull ScenarioType scenarioType,
                                  @NotNull ActionStructure structure,
                                  @Nullable BiConsumer<CompiledAction, Throwable> reportErrorTo,
                                  @Nullable BiConsumer<ActionResult, ScenarioType> onSuccess)
    {
        StructureSerializer serializer = engine.getManager().getRegistry().getScenarioFileManager().getSerializer();
        Action action = getActionByName(structure.getType());
        if (action == null)
            throw new IllegalArgumentException("Action " + structure.getType() + " is not found.");
        else if (action instanceof NegateAction)
            return processNegateAction(engine, serializer, (NegateAction) action, structure, reportErrorTo, onSuccess);


        InputBoard argument = action.getInputBoard(scenarioType);
        if (structure.getArguments() != null)
            argument.compile(serializer, structure.getArguments());

        if (!argument.hasUnresolvedReferences())
            argument.validate();

        return new CompiledActionImpl(
                action,
                new ActionContextImpl(engine, argument, engine.getPlugin().getLogger()),
                structure,
                reportErrorTo,
                onSuccess
        );
    }

    @Override
    public @NotNull List<? extends Action> getRegisteredActions()
    {
        return Collections.unmodifiableList(ACTIONS);
    }

    @Override
    public @NotNull <T extends Action> T findAction(@NotNull Class<? extends T> actionClass)
    {
        for (Action action : ACTIONS)
            if (action.getClass().equals(actionClass))
                //noinspection unchecked
                return (T) action;

        throw new IllegalArgumentException("Action " + actionClass.getName() + " is not found.");
    }
}
