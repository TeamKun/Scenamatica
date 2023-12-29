package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class PlayerDeathAction extends AbstractPlayerAction
        implements Executable, Requireable, Watchable
{
    public static final String KEY_ACTION_NAME = "player_death";
    public static final InputToken<PlayerSpecifier> IN_KILLER = ofInput(
            "killer",
            PlayerSpecifier.class,
            ofPlayer()
    );
    public static final InputToken<String> IN_DEATH_MESSAGE = ofInput(
            "deathMessage",
            String.class
    );
    public static final InputToken<Integer> IN_NEW_EXP = ofInput(
            "exp",
            Integer.class
    );
    public static final InputToken<Integer> IN_NEW_LEVEL = ofInput(
            "level",
            Integer.class
    );
    public static final InputToken<Integer> IN_NEW_TOTAL_EXP = ofInput(
            "totalExp",
            Integer.class
    );
    public static final InputToken<Boolean> IN_KEEP_LEVEL = ofInput(
            "keepLevel",
            Boolean.class
    );
    public static final InputToken<Boolean> IN_KEEP_INVENTORY = ofInput(
            "keepInventory",
            Boolean.class
    );
    public static final InputToken<Boolean> IN_DO_EXP_DROP = ofInput(
            "doExpDrop",
            Boolean.class
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player target = selectTarget(ctxt);

        ctxt.runIfHasInput(IN_KILLER, killerSpecifier -> {
            Player killer = killerSpecifier.selectTarget(ctxt.getContext())
                    .orElseThrow(() -> new IllegalStateException("Cannot select target for this action, please specify target with valid specifier."));
            target.setKiller(killer);
        });

        target.setHealth(0);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        // assert !(event instanceof PlayerEvent);

        assert event instanceof PlayerDeathEvent;
        PlayerDeathEvent e = (PlayerDeathEvent) event;

        return ctxt.ifHasInput(IN_TARGET, target -> target.checkMatchedPlayer(e.getEntity()))
                && ctxt.ifHasInput(IN_KILLER, killer -> killer.checkMatchedPlayer(e.getEntity().getKiller()))
                && ctxt.ifHasInput(IN_DEATH_MESSAGE, msg -> Objects.equals(msg, e.getDeathMessage()))
                && ctxt.ifHasInput(IN_NEW_EXP, exp -> exp == e.getNewExp())
                && ctxt.ifHasInput(IN_NEW_LEVEL, level -> level == e.getNewLevel())
                && ctxt.ifHasInput(IN_NEW_TOTAL_EXP, totalExp -> totalExp == e.getNewTotalExp())
                && ctxt.ifHasInput(IN_KEEP_LEVEL, keepLevel -> keepLevel == e.getKeepLevel())
                && ctxt.ifHasInput(IN_KEEP_INVENTORY, keepInventory -> keepInventory == e.getKeepInventory())
                && ctxt.ifHasInput(IN_DO_EXP_DROP, doExpDrop -> doExpDrop == e.shouldDropExperience());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerDeathEvent.class
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Player targetPlayer = selectTarget(ctxt);
        Player actualKiller = targetPlayer.getKiller();

        return targetPlayer.isDead()
                && ctxt.ifHasInput(IN_KILLER, killer -> killer.checkMatchedPlayer(actualKiller));
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(
                        IN_KILLER
                );
        if (type != ScenarioType.CONDITION_REQUIRE)
            board.registerAll(IN_DEATH_MESSAGE, IN_NEW_EXP, IN_NEW_LEVEL, IN_NEW_TOTAL_EXP, IN_KEEP_LEVEL, IN_KEEP_INVENTORY, IN_DO_EXP_DROP);

        return board;
    }
}
