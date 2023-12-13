package org.kunlab.scenamatica.action.actions.player;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.NamespaceUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlayerAdvancementAction
        extends AbstractPlayerAction<PlayerAdvancementAction.Argument>
        implements Executable<PlayerAdvancementAction.Argument>, Watchable<PlayerAdvancementAction.Argument>, Requireable<PlayerAdvancementAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_advancement";

    // 進捗の Criterion を付与するアクションと, 進捗を完了させるアクションを統合した。
    // 脚注： Criterion => 単数, Criteria => 複数 -- ギリシャ語による。

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable PlayerAdvancementAction.Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player target = argument.getTarget(engine);
        Advancement advancement = Bukkit.getAdvancement(argument.getAdvancement());
        if (advancement == null)
            throw new IllegalArgumentException("Advancement not found: " + argument.getAdvancement());

        AdvancementProgress progress = target.getAdvancementProgress(advancement);

        if (argument.getCriterion() != null)  // Criteria を指定している場合は, その Criteria を付与するアクションになる。
            progress.awardCriteria(argument.getCriterion());
        else  // 指定していないので, 進捗を完了させる。
            progress.getRemainingCriteria().forEach(progress::awardCriteria);
    }

    @Override
    public boolean isFired(@NotNull PlayerAdvancementAction.Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        argument = this.requireArgsNonNull(argument);

        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        if (event instanceof PlayerAdvancementDoneEvent)  // 進捗を完了させるアクションの場合
        {
            PlayerAdvancementDoneEvent e = (PlayerAdvancementDoneEvent) event;

            NamespacedKey expectedAdv = argument.getAdvancement();
            return expectedAdv == null || e.getAdvancement().getKey().equals(expectedAdv);
        }
        else  // 進捗の Criterion を付与するアクションの場合
        {
            assert event instanceof PlayerAdvancementCriterionGrantEvent;
            PlayerAdvancementCriterionGrantEvent e = (PlayerAdvancementCriterionGrantEvent) event;

            String criteria = argument.getCriterion();

            NamespacedKey expectedAdv = argument.getAdvancement();
            return (expectedAdv == null || e.getAdvancement().getKey().equals(expectedAdv))
                    && (criteria == null || e.getCriterion().equals(criteria));
        }
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Arrays.asList(
                PlayerAdvancementDoneEvent.class,
                PlayerAdvancementCriterionGrantEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        NamespacedKey advancement = null;
        if (map.containsKey(Argument.KEY_ADVANCEMENT))
            advancement = NamespaceUtils.fromString((String) map.get(Argument.KEY_ADVANCEMENT));

        String criteria = null;
        if (map.containsKey(Argument.KEY_CRITERIA))
            criteria = (String) map.get(Argument.KEY_CRITERIA);

        return new Argument(
                super.deserializeTarget(map, serializer),
                advancement,
                criteria
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable PlayerAdvancementAction.Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        Advancement advancement = Bukkit.getAdvancement(argument.getAdvancement());
        if (advancement == null)
            throw new IllegalArgumentException("Advancement not found: " + argument.getAdvancement());

        AdvancementProgress progress = argument.getTarget(engine).getAdvancementProgress(advancement);

        if (argument.getCriterion() == null)  // 進捗を完了させるアクションの場合
            return progress.isDone();
        else  // 進捗の Criterion を付与するアクションの場合
            return progress.getAwardedCriteria().contains(argument.getCriterion());
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_ADVANCEMENT = "advancement";
        public static final String KEY_CRITERIA = "criteria";

        NamespacedKey advancement;
        String criterion;

        public Argument(PlayerSpecifier target, NamespacedKey advancement, String criterion)
        {
            super(target);
            this.advancement = advancement;
            this.criterion = criterion;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument casted = (Argument) argument;

            return super.isSame(argument)
                    && (this.advancement == null || this.advancement.equals(casted.advancement))
                    && (this.criterion == null || this.criterion.equals(casted.criterion));
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);

            switch (type)
            {
                case CONDITION_REQUIRE:
                    /* fall through */
                case ACTION_EXECUTE:
                    this.ensureCanProvideTarget();
                    ensurePresent(KEY_ADVANCEMENT, this.advancement);
                    break;
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_ADVANCEMENT, this.advancement,
                    KEY_CRITERIA, this.criterion
            );
        }
    }
}
