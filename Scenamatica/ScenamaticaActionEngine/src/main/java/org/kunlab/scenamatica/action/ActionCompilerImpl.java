package org.kunlab.scenamatica.action;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.scenamatica.NegateAction;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.RunAs;
import org.kunlab.scenamatica.enums.RunOn;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioCompilationErrorException;
import org.kunlab.scenamatica.interfaces.action.ActionCompiler;
import org.kunlab.scenamatica.interfaces.action.ActionLoader;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.LoadedAction;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.scenario.ActionStructure;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ActionCompilerImpl implements ActionCompiler
{
    private final ActionLoader loader;

    public ActionCompilerImpl(ActionLoader loader)
    {
        this.loader = loader;
    }

    private CompiledActionImpl processNegateAction(
            ScenarioEngine engine,
            RunOn runOn,
            RunAs runAs,
            StructureSerializer serializer,
            NegateAction action,
            ActionStructure structure,
            BiConsumer<CompiledAction, Throwable> reportErrorTo,
            BiConsumer<ActionResult, ScenarioType> onSuccess)
            throws ScenarioCompilationErrorException
    {
        Map<String, Object> arguments = structure.getArguments();
        if (arguments == null)
            throw new ScenarioCompilationErrorException(
                    engine,
                    LangProvider.get(
                            "scenario.compiler.action.error.negate.argumentsNeeded",
                            MsgArgs.of("negateActionName", NegateAction.ACTION_NAME)
                    )
            );

        String actionName = MapUtils.getOrNull(arguments, NegateAction.KEY_IN_ACTION);
        if (actionName == null)
            throw new ScenarioCompilationErrorException(
                    engine,
                    LangProvider.get(
                            "scenario.compiler.action.error.negate.targetActionNameNeeded",
                            MsgArgs.of("negateActionName", NegateAction.ACTION_NAME)
                    )
            );

        LoadedAction<?> actionToBeNegated = this.loader.getActionByName(actionName);
        if (actionToBeNegated == null)
            throw new ScenarioCompilationErrorException(
                    engine,
                    LangProvider.get(
                            "scenario.compiler.action.error.undefinedAction",
                            MsgArgs.of("actionName", actionName)
                                    .add("actionSuggestion", this.loader.getMostSimilarActionByName(structure.getType()))
                    )
            );
        else if (!actionToBeNegated.isRequireable())
            throw new ScenarioCompilationErrorException(
                    engine,
                    LangProvider.get(
                            "scenario.compiler.action.error.scenarioTypeViolation.require",
                            MsgArgs.of("actionName", actionName)
                    )
            );

        InputBoard argument = actionToBeNegated.getInstance().getInputBoard(ScenarioType.CONDITION_REQUIRE);
        if (arguments.containsKey(NegateAction.KEY_IN_ARGUMENTS))
            argument.compile(serializer, MapUtils.checkAndCastMap(arguments.get(NegateAction.KEY_IN_ARGUMENTS)));

        InputBoard negateArgument = action.getInputBoard(ScenarioType.CONDITION_REQUIRE);
        negateArgument.compile(serializer, new HashMap<String, Object>()
        {{
            this.put(NegateAction.KEY_IN_ACTION, actionToBeNegated.getInstance());
            this.put(NegateAction.KEY_IN_ARGUMENTS, new ActionContextImpl(engine, runOn, runAs, argument, engine.getPlugin().getLogger()));
        }});

        return new CompiledActionImpl(
                action,
                new ActionContextImpl(engine, runOn, runAs, negateArgument, engine.getPlugin().getLogger()),
                structure,
                reportErrorTo,
                onSuccess
        );
    }

    @Override
    public CompiledAction compile(@NotNull ScenarioEngine engine,
                                  @NotNull ScenarioType scenarioType,
                                  @NotNull RunOn runOn,
                                  @NotNull RunAs runAs,
                                  @NotNull ActionStructure structure,
                                  @Nullable BiConsumer<CompiledAction, Throwable> reportErrorTo,
                                  @Nullable BiConsumer<ActionResult, ScenarioType> onSuccess)
            throws ScenarioCompilationErrorException
    {
        StructureSerializer serializer = engine.getManager().getRegistry().getScenarioFileManager().getSerializer();
        LoadedAction<?> action = this.loader.getActionByName(structure.getType());
        if (action == null)
            throw new ScenarioCompilationErrorException(
                    engine,
                    LangProvider.get(
                            "scenario.compiler.action.error.undefinedAction",
                            MsgArgs.of("actionName", structure.getType())
                                    .add("actionSuggestion", this.loader.getMostSimilarActionByName(structure.getType()))
                    )
            );
        else if (action.isRequireable() && NegateAction.class.isAssignableFrom(action.getActionClass())) // negate 用の特別処理
            return this.processNegateAction(engine, runOn, runAs, serializer, (NegateAction) action.getInstance(), structure, reportErrorTo, onSuccess);


        InputBoard argument = action.getInstance().getInputBoard(scenarioType);
        if (structure.getArguments() != null)
            argument.compile(serializer, structure.getArguments());

        if (!argument.hasUnresolvedReferences())
            argument.validate();

        return new CompiledActionImpl(
                action.getInstance(),
                new ActionContextImpl(engine, runOn, runAs, argument, engine.getPlugin().getLogger()),
                structure,
                reportErrorTo,
                onSuccess
        );
    }
}
