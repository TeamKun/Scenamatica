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
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

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

    private static Advancement retrieveAdvancement(@NotNull ActionContext ctxt)
    {
        NamespacedKey advKey = ctxt.input(IN_ADVANCEMENT);
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
    public void execute(@NotNull ActionContext ctxt)
    {
        Player target = selectTarget(ctxt);
        Advancement advancement = retrieveAdvancement(ctxt);
        AdvancementProgress progress = target.getAdvancementProgress(advancement);

        if (ctxt.hasInput(IN_CRITERION))  // Criteria を指定している場合は, その Criteria を付与するアクションになる。
            progress.awardCriteria(ctxt.input(IN_CRITERION));
        else  // 指定していないので, 進捗を完了させる。
            progress.getRemainingCriteria().forEach(progress::awardCriteria);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        if (event instanceof PlayerAdvancementDoneEvent)  // 進捗を完了させるアクションの場合
        {
            PlayerAdvancementDoneEvent e = (PlayerAdvancementDoneEvent) event;

            return ctxt.ifHasInput(IN_ADVANCEMENT, e.getAdvancement().getKey()::equals);
        }
        else  // 進捗の Criterion を付与するアクションの場合
        {
            assert event instanceof PlayerAdvancementCriterionGrantEvent;
            PlayerAdvancementCriterionGrantEvent e = (PlayerAdvancementCriterionGrantEvent) event;

            return ctxt.ifHasInput(IN_ADVANCEMENT, e.getAdvancement().getKey()::equals)
                    && ctxt.ifHasInput(IN_CRITERION, e.getCriterion()::equals);
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
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Advancement advancement = retrieveAdvancement(ctxt);
        AdvancementProgress progress = selectTarget(ctxt).getAdvancementProgress(advancement);

        if (ctxt.hasInput(IN_CRITERION))  // 進捗を完了させるアクションの場合
            return progress.getAwardedCriteria().contains(ctxt.input(IN_CRITERION));
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
