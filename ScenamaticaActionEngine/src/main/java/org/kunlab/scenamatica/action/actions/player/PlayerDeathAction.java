package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.specifiers.PlayerSpecifierImpl;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.PlayerSpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerDeathAction extends AbstractPlayerAction<PlayerDeathAction.Argument>
        implements Executable<PlayerDeathAction.Argument>, Requireable<PlayerDeathAction.Argument>, Watchable<PlayerDeathAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_death";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable PlayerDeathAction.Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player target = argument.getTarget(engine);
        if (argument.getKiller() != null)
        {
            Player killer = argument.getKiller().selectTarget(engine.getContext());
            target.setKiller(killer);
        }

        target.setHealth(0);
    }

    @Override
    public boolean isFired(@NotNull PlayerDeathAction.Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        // PlayerDeathEvent はプレイヤが死んだ時に発生するイベントであるが, PlayerEvent を継承していないため, super でのチェックができない

        assert event instanceof PlayerDeathEvent;
        PlayerDeathEvent e = (PlayerDeathEvent) event;

        return this.checkTargetAndKiller(argument, e)
                && this.checkDeathMessage(argument, e)
                && this.checkExp(argument, e)
                && this.checkLevel(argument, e)
                && this.checkTotalExp(argument, e)
                && this.checkKeepLevel(argument, e)
                && this.checkKeepInventory(argument, e)
                && this.checkDoExpDrop(argument, e);
    }

    private boolean checkTargetAndKiller(@NotNull PlayerDeathAction.Argument argument, @NotNull PlayerDeathEvent event)
    {
        Player target = event.getEntity();
        Player killer = target.getKiller();


        return (argument.canProvideTarget() && argument.checkMatchedPlayer(target))
                && (argument.getKiller() == null || argument.getKiller().checkMatchedPlayer(killer));
    }

    private boolean checkDeathMessage(@NotNull PlayerDeathAction.Argument argument, @NotNull PlayerDeathEvent event)
    {
        String deathMessage = argument.getDeathMessage();
        if (deathMessage == null)
            return true;
        //noinspection deprecation
        return Objects.equals(deathMessage, event.getDeathMessage());
    }

    private boolean checkExp(@NotNull PlayerDeathAction.Argument argument, @NotNull PlayerDeathEvent event)
    {
        int exp = argument.getNewExp();
        if (exp == -1)
            return true;
        return exp == event.getNewExp();
    }

    private boolean checkLevel(@NotNull PlayerDeathAction.Argument argument, @NotNull PlayerDeathEvent event)
    {
        int level = argument.getNewLevel();
        if (level == -1)
            return true;
        return level == event.getNewLevel();
    }

    private boolean checkTotalExp(@NotNull PlayerDeathAction.Argument argument, @NotNull PlayerDeathEvent event)
    {
        int totalExp = argument.getNewTotalExp();
        if (totalExp == -1)
            return true;
        return totalExp == event.getNewTotalExp();
    }

    private boolean checkKeepLevel(@NotNull PlayerDeathAction.Argument argument, @NotNull PlayerDeathEvent event)
    {
        Boolean keepLevel = argument.getKeepLevel();
        if (keepLevel == null)
            return true;
        return keepLevel == event.getKeepLevel();
    }

    private boolean checkKeepInventory(@NotNull PlayerDeathAction.Argument argument, @NotNull PlayerDeathEvent event)
    {
        Boolean keepInventory = argument.getKeepInventory();
        if (keepInventory == null)
            return true;
        return keepInventory == event.getKeepInventory();
    }

    private boolean checkDoExpDrop(@NotNull PlayerDeathAction.Argument argument, @NotNull PlayerDeathEvent event)
    {
        Boolean doExpDrop = argument.getDoExpDrop();
        if (doExpDrop == null)
            return true;
        return doExpDrop == event.shouldDropExperience();
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerDeathEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        MapUtils.checkTypeIfContains(map, Argument.KEY_KILLER, String.class);
        MapUtils.checkTypeIfContains(map, Argument.KEY_DEATH_MESSAGE, String.class);
        MapUtils.checkTypeIfContains(map, Argument.KEY_NEW_EXP, Integer.class);
        MapUtils.checkTypeIfContains(map, Argument.KEY_NEW_LEVEL, Integer.class);
        MapUtils.checkTypeIfContains(map, Argument.KEY_NEW_TOTAL_EXP, Integer.class);
        MapUtils.checkTypeIfContains(map, Argument.KEY_KEEP_LEVEL, Boolean.class);
        MapUtils.checkTypeIfContains(map, Argument.KEY_KEEP_INVENTORY, Boolean.class);
        MapUtils.checkTypeIfContains(map, Argument.KEY_DO_EXP_DROP, Boolean.class);

        PlayerSpecifier killer = PlayerSpecifierImpl.tryDeserializePlayer(map.get(Argument.KEY_KILLER), serializer);

        String deathMessage = MapUtils.getOrNull(map, "deathMessage");

        int newExp = MapUtils.getOrDefault(map, "exp", -1);
        int newLevel = MapUtils.getOrDefault(map, "level", -1);
        int newTotalExp = MapUtils.getOrDefault(map, "totalExp", -1);
        Boolean keepLevel = MapUtils.getOrNull(map, "keepLevel");
        Boolean keepInventory = MapUtils.getOrNull(map, "keepInventory");
        Boolean doExpDrop = MapUtils.getOrNull(map, "doExpDrop");

        return new Argument(
                super.deserializeTarget(map, serializer),
                killer,
                deathMessage,
                newExp,
                newLevel,
                newTotalExp,
                keepLevel,
                keepInventory,
                doExpDrop
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable PlayerDeathAction.Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        Player targetPlayer = argument.getTarget(engine);
        Player actualKiller = targetPlayer.getKiller();
        PlayerSpecifier expectedKiller = argument.getKiller();

        return targetPlayer.isDead() && (expectedKiller == null || expectedKiller.checkMatchedPlayer(actualKiller));
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_KILLER = "killer";
        public static final String KEY_DEATH_MESSAGE = "deathMessage";
        public static final String KEY_NEW_EXP = "exp";
        public static final String KEY_NEW_LEVEL = "level";
        public static final String KEY_NEW_TOTAL_EXP = "totalExp";
        public static final String KEY_KEEP_LEVEL = "keepLevel";
        public static final String KEY_KEEP_INVENTORY = "keepInventory";
        public static final String KEY_DO_EXP_DROP = "doExpDrop";

        PlayerSpecifier killer;
        @Nullable
        String deathMessage;
        int newExp;
        int newLevel;
        int newTotalExp;
        Boolean keepLevel;
        Boolean keepInventory;
        Boolean doExpDrop;

        public Argument(PlayerSpecifier target, @Nullable PlayerSpecifier killer, @Nullable String deathMessage, int newExp, int newLevel, int newTotalExp, Boolean keepLevel, Boolean keepInventory, Boolean doExpDrop)
        {
            super(target);
            this.killer = killer;
            this.deathMessage = deathMessage;
            this.newExp = newExp;
            this.newLevel = newLevel;
            this.newTotalExp = newTotalExp;
            this.keepLevel = keepLevel;
            this.keepInventory = keepInventory;
            this.doExpDrop = doExpDrop;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;
            Argument a = (Argument) argument;

            return this.checkTargetAndKiller(a)
                    && this.checkDeathMessage(a)
                    && this.checkExpAndLevel(a)
                    && this.checkInventory(a);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            switch (type)
            {
                case ACTION_EXECUTE:
                    this.ensureCanProvideTarget();
                    break;
                case CONDITION_REQUIRE:
                    ensureNotPresent(Argument.KEY_DEATH_MESSAGE, this.deathMessage);
                    ensureEquals(Argument.KEY_NEW_EXP, this.newExp, -1);
                    ensureEquals(Argument.KEY_NEW_LEVEL, this.newLevel, -1);
                    ensureEquals(Argument.KEY_NEW_TOTAL_EXP, this.newTotalExp, -1);
                    ensureNotPresent(Argument.KEY_KEEP_LEVEL, this.keepLevel);
                    ensureNotPresent(Argument.KEY_KEEP_INVENTORY, this.keepInventory);
                    ensureNotPresent(Argument.KEY_DO_EXP_DROP, this.doExpDrop);
                    break;
            }
        }

        private boolean checkTargetAndKiller(@NotNull PlayerDeathAction.Argument argument)
        {
            return this.isSameTarget(argument) &&
                    Objects.equals(this.killer, argument.killer);
        }

        private boolean checkDeathMessage(@NotNull PlayerDeathAction.Argument argument)
        {
            return Objects.equals(this.deathMessage, argument.deathMessage);
        }

        private boolean checkExpAndLevel(@NotNull PlayerDeathAction.Argument argument)
        {
            return this.newExp == argument.newExp
                    && this.newLevel == argument.newLevel
                    && this.newTotalExp == argument.newTotalExp
                    && this.keepLevel == argument.keepLevel
                    && this.doExpDrop == argument.doExpDrop;
        }

        private boolean checkInventory(@NotNull PlayerDeathAction.Argument argument)
        {
            return this.keepInventory == argument.keepInventory;
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_KILLER, this.killer,
                    KEY_DEATH_MESSAGE, this.deathMessage,
                    KEY_NEW_EXP, this.newExp,
                    KEY_NEW_LEVEL, this.newLevel,
                    KEY_NEW_TOTAL_EXP, this.newTotalExp,
                    KEY_KEEP_LEVEL, this.keepLevel,
                    KEY_KEEP_INVENTORY, this.keepInventory,
                    KEY_DO_EXP_DROP, this.doExpDrop
            );
        }
    }
}
