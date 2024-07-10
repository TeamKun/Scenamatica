package org.kunlab.scenamatica.action.actions.base.player;

import com.destroystokyo.paper.event.player.PlayerAdvancementCriterionGrantEvent;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.Admonition;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.AdmonitionType;
import org.kunlab.scenamatica.commons.utils.NamespaceUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

import java.util.Arrays;
import java.util.List;

@Action("player_advancement")
@ActionDoc(
        name = "プレイヤの進捗",
        description = "プレイヤの進捗を設定します。",

        executable = "プレイヤの進捗を達成するか、達成度を設定します。",
        expectable = "プレイヤの進捗が達成、または達成度が設定されすることを期待します。",
        requireable = "プレイヤが指定された進捗を達成しているか、または達成度が設定されていることを要求します。",

        outputs = {
                @OutputDoc(
                        name = PlayerAdvancementAction.KEY_OUT_ADVANCEMENT,
                        description = "達成した進捗のキーです。",
                        type = NamespacedKey.class
                ),
                @OutputDoc(
                        name = PlayerAdvancementAction.KEY_OUT_CRITERION,
                        description = "達成した進捗の条件です。",
                        type = String.class
                )
        },

        admonitions = {
                @Admonition(
                        type = AdmonitionType.INFORMATION,
                        content = "このアクションは進捗自体を達成する機能と、進捗の達成度を変更する機能を包含しています。  \n" +
                                "`criterion` を指定せずに実行すると、**残っている達成条件を全て達成**します。一方で指定した場合は、指定した達成条件のみを達成します。\n" +
                                "\n" +
                                "実行期待機能においても、 `criterion` を指定せずに実行すると、進捗自体を達成しているか判定します。  \n" +
                                "一方で指定した場合は、指定した進捗の指定した達成条件を達成しているか判定します。"
                )
        }
)
public class PlayerAdvancementAction
        extends AbstractPlayerAction
        implements Executable, Expectable, Requireable
{
    // 進捗の Criterion を付与するアクションと, 進捗を完了させるアクションを統合した。
    // 脚注： Criterion => 単数, Criteria => 複数 -- ギリシャ語による。

    @InputDoc(
            name = "advancement",
            description = "進捗を指定します。",
            type = NamespacedKey.class
    )
    public static final InputToken<NamespacedKey> IN_ADVANCEMENT = ofInput(
            "advancement",
            NamespacedKey.class,
            ofTraverser(String.class, (ser, str) -> NamespaceUtils.fromString(str))
    );
    @InputDoc(
            name = "criterion",
            description = "進捗の条件を指定します。",
            type = String.class
    )
    public static final InputToken<String> IN_CRITERION = ofInput(
            "criterion",
            String.class
    );

    public static final String KEY_OUT_ADVANCEMENT = "advancement";
    public static final String KEY_OUT_CRITERION = "criterion";

    private static Advancement retrieveAdvancement(@NotNull ActionContext ctxt)
    {
        NamespacedKey advKey = ctxt.input(IN_ADVANCEMENT);
        Advancement advancement = Bukkit.getAdvancement(advKey);
        if (advancement == null)
            throw new IllegalArgumentException("Advancement not found: " + advKey);

        return advancement;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player target = selectTarget(ctxt);
        Advancement advancement = retrieveAdvancement(ctxt);
        AdvancementProgress progress = target.getAdvancementProgress(advancement);

        if (ctxt.hasInput(IN_CRITERION))  // Criteria を指定している場合は, その Criteria を付与するアクションになる。
        {
            String criterion = ctxt.input(IN_CRITERION);
            this.makeOutputs(ctxt, target, advancement.getKey(), criterion);
            progress.awardCriteria(criterion);
        }
        else  // 指定していないので, 進捗を完了させる。
        {
            this.makeOutputs(ctxt, target, advancement.getKey(), null);
            progress.getRemainingCriteria().forEach(progress::awardCriteria);
        }
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        boolean result;
        NamespacedKey advancement;
        String criterion = null;
        if (event instanceof PlayerAdvancementDoneEvent)  // 進捗を完了させるアクションの場合
        {
            PlayerAdvancementDoneEvent e = (PlayerAdvancementDoneEvent) event;

            advancement = e.getAdvancement().getKey();
            result = ctxt.ifHasInput(IN_ADVANCEMENT, e.getAdvancement().getKey()::equals);
        }
        else  // 進捗の Criterion を付与するアクションの場合
        {
            assert event instanceof PlayerAdvancementCriterionGrantEvent;
            PlayerAdvancementCriterionGrantEvent e = (PlayerAdvancementCriterionGrantEvent) event;

            advancement = e.getAdvancement().getKey();
            criterion = e.getCriterion();
            result = ctxt.ifHasInput(IN_ADVANCEMENT, e.getAdvancement().getKey()::equals)
                    && ctxt.ifHasInput(IN_CRITERION, e.getCriterion()::equals);
        }

        if (result)
            this.makeOutputs(ctxt, ((PlayerEvent) event).getPlayer(), advancement, criterion);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull NamespacedKey advancement, @Nullable String criterion)
    {
        ctxt.output(KEY_OUT_ADVANCEMENT, advancement);
        if (criterion != null)
            ctxt.output(KEY_OUT_CRITERION, criterion);
        super.makeOutputs(ctxt, player);
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
