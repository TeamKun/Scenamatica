package org.kunlab.scenamatica.action.actions.player;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
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
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Player target = selectTarget(argument, engine);

        argument.runIfPresent(IN_KILLER, killerSpecifier -> {
            Player killer = killerSpecifier.selectTarget(engine.getContext())
                    .orElseThrow(() -> new IllegalStateException("Cannot select target for this action, please specify target with valid specifier."));
            target.setKiller(killer);
        });

        target.setHealth(0);
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        // assert !(event instanceof PlayerEvent);

        assert event instanceof PlayerDeathEvent;
        PlayerDeathEvent e = (PlayerDeathEvent) event;

        return argument.ifPresent(IN_TARGET, target -> target.checkMatchedPlayer(e.getEntity()))
                && argument.ifPresent(IN_KILLER, killer -> killer.checkMatchedPlayer(e.getEntity().getKiller()))
                && argument.ifPresent(IN_DEATH_MESSAGE, msg -> Objects.equals(msg, e.getDeathMessage()))
                && argument.ifPresent(IN_NEW_EXP, exp -> exp == e.getNewExp())
                && argument.ifPresent(IN_NEW_LEVEL, level -> level == e.getNewLevel())
                && argument.ifPresent(IN_NEW_TOTAL_EXP, totalExp -> totalExp == e.getNewTotalExp())
                && argument.ifPresent(IN_KEEP_LEVEL, keepLevel -> keepLevel == e.getKeepLevel())
                && argument.ifPresent(IN_KEEP_INVENTORY, keepInventory -> keepInventory == e.getKeepInventory())
                && argument.ifPresent(IN_DO_EXP_DROP, doExpDrop -> doExpDrop == e.shouldDropExperience());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerDeathEvent.class
        );
    }

    @Override
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        Player targetPlayer = selectTarget(argument, engine);
        Player actualKiller = targetPlayer.getKiller();

        return targetPlayer.isDead()
                && argument.ifPresent(IN_KILLER, killer -> killer.checkMatchedPlayer(actualKiller));
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
