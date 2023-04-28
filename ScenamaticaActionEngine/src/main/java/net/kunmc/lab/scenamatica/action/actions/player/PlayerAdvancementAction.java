package net.kunmc.lab.scenamatica.action.actions.player;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.commons.utils.NamespaceUtils;
import net.kunmc.lab.scenamatica.interfaces.action.Requireable;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlayerAdvancementAction
        extends AbstractPlayerAction<PlayerAdvancementAction.Argument>
        implements Requireable<PlayerAdvancementAction.Argument>
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

        Player target = argument.getTarget();
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

        if (!super.isFired(argument, engine, event))
            return false;

        if (event instanceof PlayerAdvancementDoneEvent)  // 進捗を完了させるアクションの場合
        {
            PlayerAdvancementDoneEvent e = (PlayerAdvancementDoneEvent) event;

            return e.getAdvancement().getKey().equals(argument.getAdvancement());
        }
        else  // 進捗の Criterion を付与するアクションの場合
        {
            assert event instanceof PlayerAdvancementCriterionGrantEvent;
            PlayerAdvancementCriterionGrantEvent e = (PlayerAdvancementCriterionGrantEvent) event;

            String criteria = argument.getCriterion();

            return e.getAdvancement().getKey().equals(argument.getAdvancement())
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
    public Argument deserializeArgument(@NotNull Map<String, Object> map)
    {
        MapUtils.checkContainsKey(map, Argument.KEY_ADVANCEMENT);

        NamespacedKey advancement = NamespaceUtils.fromString((String) map.get(Argument.KEY_ADVANCEMENT));

        String criteria = null;
        if (map.containsKey(Argument.KEY_CRITERIA))
            criteria = (String) map.get(Argument.KEY_CRITERIA);

        return new Argument(
                super.deserializeTarget(map),
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

        AdvancementProgress progress = argument.getTarget().getAdvancementProgress(advancement);

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

        @NotNull
        NamespacedKey advancement;
        @Nullable
        String criterion;

        public Argument(@NotNull String target, @NotNull NamespacedKey advancement, @Nullable String criterion)
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

            return this.advancement.equals(casted.advancement);
        }

        @Override
        public String getArgumentString()
        {
            StringBuilder builder = new StringBuilder("advancement='")
                    .append(this.advancement)
                    .append("'");

            if (this.criterion != null)
                builder.append(", criteria='").append(this.criterion).append("'");

            return builder.toString();
        }
    }
}
