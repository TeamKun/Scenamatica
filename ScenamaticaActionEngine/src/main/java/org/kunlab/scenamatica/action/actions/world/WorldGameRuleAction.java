package org.kunlab.scenamatica.action.actions.world;

import io.papermc.paper.event.world.WorldGameRuleChangeEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.GameRule;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WorldGameRuleAction extends AbstractWorldAction<WorldGameRuleAction.Argument>
        implements Executable<WorldGameRuleAction.Argument>, Requireable<WorldGameRuleAction.Argument>
{
    public static final String KEY_ACTION_NAME = "world_game_rule";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        World world = argument.getWorldNonNull(engine);
        GameRule<?> rule = argument.getGameRule();
        String value = argument.getValue();
        assert value != null;

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

        if (!success)
            throw new IllegalArgumentException("Failed to set the game rule: Attempted to set " + rule.getName() + " to " + value);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        assert event instanceof WorldGameRuleChangeEvent;
        WorldGameRuleChangeEvent e = (WorldGameRuleChangeEvent) event;

        if (!super.isFired(argument, engine, event))
            return false;

        return (argument.getGameRule() == null || e.getGameRule().getName().equalsIgnoreCase(argument.getGameRule().getName()))
                && (argument.getValue() == null || e.getValue().equalsIgnoreCase(argument.getValue()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WorldGameRuleChangeEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        MapUtils.checkContainsKey(map, Argument.KEY_GAME_RULE);

        GameRule<?> rule = GameRule.getByName(map.get(Argument.KEY_GAME_RULE).toString());
        if (rule == null)
            throw new IllegalArgumentException("Invalid game rule: " + map.get(Argument.KEY_GAME_RULE));

        String value;
        if (map.containsKey(Argument.KEY_VALUE))
            value = map.get(Argument.KEY_VALUE).toString();  // ClassCastException 回避
        else
            value = null;

        return new Argument(
                this.deserializeWorld(map),
                rule,
                value
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        World world = argument.getWorldNonNull(engine);
        GameRule<?> rule = argument.getGameRule();
        Class<?> type = rule.getType();
        String expectedValue = argument.getValue();
        assert expectedValue != null;

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

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractWorldActionArgument
    {
        public static final String KEY_GAME_RULE = "rule";
        public static final String KEY_VALUE = "value";

        GameRule<?> gameRule;
        String value;

        public Argument(@Nullable NamespacedKey worldRef, @NotNull GameRule<?> gameRule, @Nullable String value)
        {
            super(worldRef);
            this.gameRule = gameRule;
            this.value = value;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSameWorld(arg)
                    && Objects.equals(this.gameRule, arg.gameRule)
                    && Objects.equals(this.value, arg.value);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            switch (type)
            {
                case ACTION_EXECUTE:
                    /* fallthrough */
                case CONDITION_REQUIRE:
                    ensurePresent(KEY_GAME_RULE, this.gameRule);
                    break;
                case ACTION_EXPECT:
                    ensurePresent(KEY_VALUE, this.value);
                    break;
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_GAME_RULE, this.gameRule.getName(),
                    KEY_VALUE, this.value
            );
        }
    }
}
