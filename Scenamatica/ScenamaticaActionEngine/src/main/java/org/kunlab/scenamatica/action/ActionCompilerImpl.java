package org.kunlab.scenamatica.action;

import net.kunmc.lab.peyangpaperutils.lang.LangProvider;
import net.kunmc.lab.peyangpaperutils.lang.MsgArgs;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.scenamatica.NegateAction;
import org.kunlab.scenamatica.enums.RunAs;
import org.kunlab.scenamatica.enums.RunOn;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioCompilationErrorException;
import org.kunlab.scenamatica.exceptions.scenario.ScenarioDefinitionErrorException;
import org.kunlab.scenamatica.exceptions.scenariofile.InvalidScenarioFileException;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.action.ActionCompiler;
import org.kunlab.scenamatica.interfaces.action.ActionLoader;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.LoadedAction;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.scenario.ActionStructure;

import java.util.HashMap;
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
            throws ScenarioCompilationErrorException, YamlParsingException
    {
        StructuredYamlNode arguments = structure.getArguments();
        if (arguments.isNullish())
            throw new ScenarioCompilationErrorException(
                    engine,
                    LangProvider.get(
                            "scenario.compiler.action.error.negate.argumentsNeeded",
                            MsgArgs.of("negateActionName", NegateAction.ACTION_NAME)
                    )
            );

        // NegateAction は, 引数をバラして上げる必要がある：引数にアクションが内包されているため。
        if (!arguments.containsKey(NegateAction.KEY_IN_ACTION))
            throw new ScenarioCompilationErrorException(
                    engine,
                    LangProvider.get(
                            "scenario.compiler.action.error.negate.targetActionNameNeeded",
                            MsgArgs.of("negateActionName", NegateAction.ACTION_NAME)
                    )
            );
        String actionName = arguments.get(NegateAction.KEY_IN_ACTION).asString();

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

        InputBoard negateArgument;
        InputBoard argument = actionToBeNegated.getInstance().getInputBoard(ScenarioType.CONDITION_REQUIRE);
        try
        {
            if (arguments.containsKey(NegateAction.KEY_IN_ARGUMENTS))
                argument.compile(serializer, arguments.get(NegateAction.KEY_IN_ARGUMENTS));

            negateArgument = action.getInputBoard(ScenarioType.CONDITION_REQUIRE);
            negateArgument.compile(serializer, arguments.renewByMap(new HashMap<String, Object>()
            {{
                this.put(NegateAction.KEY_IN_ACTION, actionToBeNegated.getInstance());
                this.put(NegateAction.KEY_IN_ARGUMENTS, new ActionContextImpl(
                        ScenarioType.CONDITION_REQUIRE,
                        engine,
                        runOn,
                        runAs,
                        argument,
                        engine.getPlugin().getLogger()
                ));
            }}));
        }
        catch (InvalidScenarioFileException e)
        {
            assert e instanceof YamlParsingException;
            throw new ScenarioDefinitionErrorException(engine, e.getMessage(), (YamlParsingException) e);
        }

        return new CompiledActionImpl(
                action,
                new ActionContextImpl(
                        ScenarioType.CONDITION_REQUIRE,
                        engine,
                        runOn,
                        runAs,
                        negateArgument,
                        engine.getPlugin().getLogger()
                ),
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
        {
            try
            {
                return this.processNegateAction(engine, runOn, runAs, serializer, (NegateAction) action.getInstance(), structure, reportErrorTo, onSuccess);
            }
            catch (YamlParsingException e)
            {
                throw new ScenarioDefinitionErrorException(engine, e.getMessage(), e);
            }
        }


        InputBoard argument = action.getInstance().getInputBoard(scenarioType);
        if (!structure.getArguments().isNullish())
        {
            try
            {
                argument.compile(serializer, structure.getArguments());
            }
            catch (InvalidScenarioFileException e)
            {
                assert e instanceof YamlParsingException;
                throw new ScenarioDefinitionErrorException(engine, e.getMessage(), (YamlParsingException) e);
            }
        }

        if (!argument.hasUnresolvedReferences())
            argument.validate();

        return new CompiledActionImpl(
                action.getInstance(),
                new ActionContextImpl(
                        scenarioType,
                        engine,
                        runOn,
                        runAs,
                        argument,
                        engine.getPlugin().getLogger()
                ),
                structure,
                reportErrorTo,
                onSuccess
        );
    }
}
