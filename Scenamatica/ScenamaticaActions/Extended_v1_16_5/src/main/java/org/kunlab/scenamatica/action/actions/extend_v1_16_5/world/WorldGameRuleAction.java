package org.kunlab.scenamatica.action.actions.extend_v1_16_5.world;

import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.base.world.AbstractWorldAction;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.exceptions.scenario.IllegalScenarioStateException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;

import java.util.Collections;
import java.util.List;

@Action("world_game_rule")
@ActionDoc(
        name = "ゲームルールの設定",
        description = "ワールドのゲームルールを設定します。",
        events = {
                WorldGameRuleChangeEvent.class
        },

        executable = "ゲームルールを設定します。",
        expectable = "ゲームルールが設定されることを期待します。",
        requireable = "ゲームルールが設定されていることを要求します。",

        supportsSince = MCVersion.V1_16_5
)
public class WorldGameRuleAction extends AbstractWorldAction
        implements Executable, Requireable
{
    public static final InputToken<GameRule<?>> IN_GAME_RULE = ofInput(
            "rule",
            InputTypeToken.ofBased(GameRule.class),
            ofTraverser(String.class, (ser, str) -> {
                GameRule<?> rule = GameRule.getByName(str);
                if (rule == null)
                    throw new IllegalActionInputException("Unknown game rule: " + str);
                return rule;
            })
    );
    public static final InputToken<String> IN_VALUE = ofInput(
            "value",
            String.class
    );
    public static final String KEY_GAME_RULE = "rule";
    public static final String KEY_VALUE = "value";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        World world = super.getWorldNonNull(ctxt);
        GameRule<?> rule = ctxt.input(IN_GAME_RULE);
        String value = ctxt.input(IN_VALUE);

        Class<?> type = rule.getType();

        boolean success;
        assert type == Boolean.class || type == Integer.class;  // Bukkit API にはこれ以外の型は存在しない
        if (type == Boolean.class)
        {
            //noinspection unchecked - type is checked above
            GameRule<Boolean> booleanRule = (GameRule<Boolean>) rule;
            success = world.setGameRule(booleanRule, Boolean.parseBoolean(value));
        }
        else
        {
            //noinspection unchecked - type is checked above
            GameRule<Integer> integerRule = (GameRule<Integer>) rule;
            success = world.setGameRule(integerRule, Integer.parseInt(value));
        }

        if (success)
            this.makeOutputs(ctxt, world, rule, value);
        else
            throw new IllegalScenarioStateException("Unable to set the game rule: Attempted to set " + rule.getName() + " to " + value);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkFired(ctxt, event))
            return false;

        assert event instanceof WorldGameRuleChangeEvent;
        WorldGameRuleChangeEvent e = (WorldGameRuleChangeEvent) event;

        boolean result = ctxt.ifHasInput(IN_GAME_RULE, rule -> rule.getName().equalsIgnoreCase(e.getGameRule().getName()))
                && ctxt.ifHasInput(IN_VALUE, value -> value.equalsIgnoreCase(e.getValue()));

        if (result)
            this.makeOutputs(ctxt, e.getWorld(), e.getGameRule(), e.getValue());

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull World world, @NotNull GameRule<?> rule, @NotNull String value)
    {
        ctxt.output(KEY_GAME_RULE, rule);
        ctxt.output(KEY_VALUE, value);
        super.makeOutputs(ctxt, world);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WorldGameRuleChangeEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        World world = super.getWorldNonNull(ctxt);
        GameRule<?> rule = ctxt.input(IN_GAME_RULE);
        Class<?> type = rule.getType();
        String expectedValue = ctxt.input(IN_VALUE);

        assert type == Boolean.class || type == Integer.class;  // Bukkit API にはこれ以外の型は存在しない
        if (type == Boolean.class)
        {
            //noinspection unchecked - type is checked above
            GameRule<Boolean> booleanRule = (GameRule<Boolean>) rule;
            Boolean value = world.getGameRuleValue(booleanRule);
            return value != null && Boolean.parseBoolean(expectedValue) == value;
        }
        else
        {
            //noinspection unchecked - type is checked above
            GameRule<Integer> integerRule = (GameRule<Integer>) rule;
            Integer value = world.getGameRuleValue(integerRule);
            return value != null && Integer.parseInt(expectedValue) == value;
        }
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_GAME_RULE, IN_VALUE);
        if (type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
            board.requirePresent(IN_GAME_RULE, IN_VALUE);
        return board;
    }
}
