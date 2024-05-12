package org.kunlab.scenamatica.action.actions.extended_v1_16.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.base.player.AbstractPlayerAction;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier;

import java.util.Collections;
import java.util.List;

@Action(value = "player_death", supportsSince = MinecraftVersion.V1_16)
public class PlayerDeathAction extends AbstractPlayerAction
        implements Executable, Requireable, Watchable
{
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
    public static final String KEY_OUT_KILLER = "killer";
    public static final String KEY_OUT_DEATH_MESSAGE = "deathMessage";
    public static final String KEY_OUT_NEW_EXP = "exp";
    public static final String KEY_OUT_NEW_LEVEL = "level";
    public static final String KEY_OUT_NEW_TOTAL_EXP = "totalExp";
    public static final String KEY_OUT_KEEP_LEVEL = "keepLevel";
    public static final String KEY_OUT_KEEP_INVENTORY = "keepInventory";
    public static final String KEY_OUT_DO_EXP_DROP = "doExpDrop";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player target = selectTarget(ctxt);

        ctxt.runIfHasInput(IN_KILLER, killerSpecifier -> {
            Player killer = killerSpecifier.selectTarget(ctxt.getContext())
                    .orElseThrow(() -> new IllegalStateException("Cannot select target for this action, please specify target with valid specifier."));
            target.setKiller(killer);
            this.makeOutputs(ctxt, target, killer);
        });

        if (!ctxt.hasInput(IN_KILLER))
            this.makeOutputs(ctxt, target, null);
        target.setHealth(0);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        // assert !(event instanceof PlayerEvent);

        assert event instanceof PlayerDeathEvent;
        PlayerDeathEvent e = (PlayerDeathEvent) event;

        boolean result = ctxt.ifHasInput(IN_TARGET, target -> target.checkMatchedPlayer(e.getEntity()))
                && ctxt.ifHasInput(IN_KILLER, killer -> killer.checkMatchedPlayer(e.getEntity().getKiller()))
                && ctxt.ifHasInput(IN_DEATH_MESSAGE, msg -> msg.equalsIgnoreCase(e.getDeathMessage()))
                && ctxt.ifHasInput(IN_NEW_EXP, exp -> exp == e.getNewExp())
                && ctxt.ifHasInput(IN_NEW_LEVEL, level -> level == e.getNewLevel())
                && ctxt.ifHasInput(IN_NEW_TOTAL_EXP, totalExp -> totalExp == e.getNewTotalExp())
                && ctxt.ifHasInput(IN_KEEP_LEVEL, keepLevel -> keepLevel == e.getKeepLevel())
                && ctxt.ifHasInput(IN_KEEP_INVENTORY, keepInventory -> keepInventory == e.getKeepInventory())
                && ctxt.ifHasInput(IN_DO_EXP_DROP, doExpDrop -> doExpDrop == e.shouldDropExperience());

        if (result)
            this.makeOutputs(ctxt,
                    e.getEntity(),
                    e.getEntity().getKiller(), e.getDeathMessage(),
                    e.getNewExp(), e.getNewLevel(), e.getNewTotalExp(), e.getKeepLevel(), e.getKeepInventory(),
                    e.shouldDropExperience()
            );

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player target, @Nullable Player killer, @Nullable String deathMessage, @NotNull Integer newExp, @NotNull Integer newLevel, @NotNull Integer newTotalExp, @NotNull Boolean keepLevel, @NotNull Boolean keepInventory, @NotNull Boolean doExpDrop)
    {
        if (killer != null)
            ctxt.output(KEY_OUT_KILLER, killer);
        if (deathMessage != null)
            ctxt.output(KEY_OUT_DEATH_MESSAGE, deathMessage);
        ctxt.output(KEY_OUT_NEW_EXP, newExp);
        ctxt.output(KEY_OUT_NEW_LEVEL, newLevel);
        ctxt.output(KEY_OUT_NEW_TOTAL_EXP, newTotalExp);
        ctxt.output(KEY_OUT_KEEP_LEVEL, keepLevel);
        ctxt.output(KEY_OUT_KEEP_INVENTORY, keepInventory);
        ctxt.output(KEY_OUT_DO_EXP_DROP, doExpDrop);

        super.makeOutputs(ctxt, target);
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Player target, @Nullable Player killer)
    {
        if (killer != null)
            ctxt.output(KEY_OUT_KILLER, killer);

        super.makeOutputs(ctxt, target);
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
