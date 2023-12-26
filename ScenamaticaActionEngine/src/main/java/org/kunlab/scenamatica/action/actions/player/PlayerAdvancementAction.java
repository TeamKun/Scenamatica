package org.kunlab.scenamatica.action.actions.player;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.NamespaceUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;

import java.util.Arrays;
import java.util.List;

public class PlayerAdvancementAction
        extends AbstractPlayerAction
        implements Executable, Watchable, Requireable
{
    public static final String KEY_ACTION_NAME = "player_advancement";

    // 進捗の Criterion を付与するアクションと, 進捗を完了させるアクションを統合した。
    // 脚注： Criterion => 単数, Criteria => 複数 -- ギリシャ語による。
    public static final InputToken<NamespacedKey> IN_ADVANCEMENT = ofInput(
            "advancement",
            NamespacedKey.class,
            ofTraverser(String.class, (ser, str) -> NamespaceUtils.fromString(str))
    );
    public static final InputToken<String> IN_CRITERION = ofInput(
            "criterion",
            String.class
    );

    private static Advancement retrieveAdvancement(@NotNull InputBoard argument)
    {
        NamespacedKey advKey = argument.get(IN_ADVANCEMENT);
        Advancement advancement = Bukkit.getAdvancement(advKey);
        if (advancement == null)
            throw new IllegalArgumentException("Advancement not found: " + advKey);

        return advancement;
    }

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Player target = selectTarget(argument, engine);
        Advancement advancement = retrieveAdvancement(argument);
        AdvancementProgress progress = target.getAdvancementProgress(advancement);

        if (argument.isPresent(IN_CRITERION))  // Criteria を指定している場合は, その Criteria を付与するアクションになる。
            progress.awardCriteria(argument.get(IN_CRITERION));
        else  // 指定していないので, 進捗を完了させる。
            progress.getRemainingCriteria().forEach(progress::awardCriteria);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        if (event instanceof PlayerAdvancementDoneEvent)  // 進捗を完了させるアクションの場合
        {
            PlayerAdvancementDoneEvent e = (PlayerAdvancementDoneEvent) event;

            return argument.ifPresent(IN_ADVANCEMENT, e.getAdvancement().getKey()::equals);
        }
        else  // 進捗の Criterion を付与するアクションの場合
        {
            assert event instanceof PlayerAdvancementCriterionGrantEvent;
            PlayerAdvancementCriterionGrantEvent e = (PlayerAdvancementCriterionGrantEvent) event;

            return argument.ifPresent(IN_ADVANCEMENT, e.getAdvancement().getKey()::equals)
                    && argument.ifPresent(IN_CRITERION, e.getCriterion()::equals);
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
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        Advancement advancement = retrieveAdvancement(argument);
        AdvancementProgress progress = selectTarget(argument, engine).getAdvancementProgress(advancement);

        if (argument.isPresent(IN_CRITERION))  // 進捗を完了させるアクションの場合
            return progress.getAwardedCriteria().contains(argument.get(IN_CRITERION));
        else  // 進捗の Criterion を付与するアクションの場合
            return progress.isDone();
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_ADVANCEMENT, IN_CRITERION);
        if (type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
            board.requirePresent(IN_ADVANCEMENT);

        return board;
    }
}
