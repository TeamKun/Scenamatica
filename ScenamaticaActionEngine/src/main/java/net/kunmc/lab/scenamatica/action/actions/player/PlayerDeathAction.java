package net.kunmc.lab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.scenamatica.action.utils.PlayerUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.action.Requireable;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerDeathAction extends AbstractPlayerAction<PlayerDeathAction.DeathArgument> implements Requireable<PlayerDeathAction.DeathArgument>
{
    public static final String KEY_ACTION_NAME = "player_death";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable DeathArgument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player target = argument.getTarget();
        String killerName = argument.getKiller();
        if (killerName != null)
        {
            Player killerPlayer = PlayerUtils.getPlayerOrThrow(killerName);
            target.setKiller(killerPlayer);
        }

        target.setHealth(0);
    }

    @Override
    public boolean isFired(@NotNull DeathArgument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.isFired(argument, engine, event))
            return false;

        assert event instanceof PlayerDeathEvent;
        PlayerDeathEvent e = (PlayerDeathEvent) event;

        return checkTargetAndKiller(argument, e)
                && checkDeathMessage(argument, e)
                && checkExp(argument, e)
                && checkLevel(argument, e)
                && checkTotalExp(argument, e)
                && checkKeepLevel(argument, e)
                && checkKeepInventory(argument, e)
                && checkDoExpDrop(argument, e);
    }

    private boolean checkTargetAndKiller(@NotNull DeathArgument argument, @NotNull PlayerDeathEvent event)
    {
        Player target = argument.getTarget();
        Player killer = PlayerUtils.getPlayerOrNull(argument.getKiller());

        UUID targetUUID = target.getUniqueId();
        UUID killerUUID = killer == null ? null: killer.getUniqueId();

        UUID eventDeathUUID = event.getEntity().getUniqueId();
        UUID eventKillerUUID = event.getEntity().getKiller() == null ? null: event.getEntity().getKiller().getUniqueId();

        return eventDeathUUID.equals(targetUUID) && Objects.equals(killerUUID, eventKillerUUID);
    }

    private boolean checkDeathMessage(@NotNull DeathArgument argument, @NotNull PlayerDeathEvent event)
    {
        String deathMessage = argument.getDeathMessage();
        if (deathMessage == null)
            return true;
        //noinspection deprecation
        return Objects.equals(deathMessage, event.getDeathMessage());
    }

    private boolean checkExp(@NotNull DeathArgument argument, @NotNull PlayerDeathEvent event)
    {
        int exp = argument.getNewExp();
        if (exp == -1)
            return true;
        return exp == event.getNewExp();
    }

    private boolean checkLevel(@NotNull DeathArgument argument, @NotNull PlayerDeathEvent event)
    {
        int level = argument.getNewLevel();
        if (level == -1)
            return true;
        return level == event.getNewLevel();
    }

    private boolean checkTotalExp(@NotNull DeathArgument argument, @NotNull PlayerDeathEvent event)
    {
        int totalExp = argument.getNewTotalExp();
        if (totalExp == -1)
            return true;
        return totalExp == event.getNewTotalExp();
    }

    private boolean checkKeepLevel(@NotNull DeathArgument argument, @NotNull PlayerDeathEvent event)
    {
        Boolean keepLevel = argument.getKeepLevel();
        if (keepLevel == null)
            return true;
        return keepLevel == event.getKeepLevel();
    }

    private boolean checkKeepInventory(@NotNull DeathArgument argument, @NotNull PlayerDeathEvent event)
    {
        Boolean keepInventory = argument.getKeepInventory();
        if (keepInventory == null)
            return true;
        return keepInventory == event.getKeepInventory();
    }

    private boolean checkDoExpDrop(@NotNull DeathArgument argument, @NotNull PlayerDeathEvent event)
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
    public DeathArgument deserializeArgument(@NotNull Map<String, Object> map)
    {
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_KILLER, String.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_DEATH_MESSAGE, String.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_NEW_EXP, Integer.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_NEW_LEVEL, Integer.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_NEW_TOTAL_EXP, Integer.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_KEEP_LEVEL, Boolean.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_KEEP_INVENTORY, Boolean.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_DO_EXP_DROP, Boolean.class);

        String killer = MapUtils.getOrNull(map, "killer");

        String deathMessage = MapUtils.getOrNull(map, "deathMessage");

        int newExp = MapUtils.getOrDefault(map, "exp", -1);
        int newLevel = MapUtils.getOrDefault(map, "level", -1);
        int newTotalExp = MapUtils.getOrDefault(map, "totalExp", -1);
        Boolean keepLevel = MapUtils.getOrNull(map, "keepLevel");
        Boolean keepInventory = MapUtils.getOrNull(map, "keepInventory");
        Boolean doExpDrop = MapUtils.getOrNull(map, "doExpDrop");

        return new DeathArgument(
                super.deserializeTarget(map),
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
    public boolean isConditionFulfilled(@Nullable DeathArgument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        String killer = argument.getKiller();

        Player targetPlayer = argument.getTarget();
        Player killerPlayer = PlayerUtils.getPlayerOrNull(killer);

        return targetPlayer.isDead() &&
                (killerPlayer == null ||
                        (targetPlayer.getKiller() != null && targetPlayer.getKiller().getUniqueId()
                                .equals(killerPlayer.getUniqueId()))
                );
    }

    @Override
    public void validateArgument(@Nullable DeathArgument argument)
    {
        argument = this.requireArgsNonNull(argument);
        this.throwIfPresent(DeathArgument.KEY_DEATH_MESSAGE, argument.getDeathMessage());
        this.throwIfNotEquals(DeathArgument.KEY_NEW_EXP, argument.getNewExp(), -1);
        this.throwIfNotEquals(DeathArgument.KEY_NEW_LEVEL, argument.getNewLevel(), -1);
        this.throwIfNotEquals(DeathArgument.KEY_NEW_TOTAL_EXP, argument.getNewTotalExp(), -1);
        this.throwIfPresent(DeathArgument.KEY_KEEP_LEVEL, argument.getKeepLevel());
        this.throwIfPresent(DeathArgument.KEY_KEEP_INVENTORY, argument.getKeepInventory());
        this.throwIfPresent(DeathArgument.KEY_DO_EXP_DROP, argument.getDoExpDrop());
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class DeathArgument extends AbstractPlayerActionArgument
    {
        public static final String KEY_KILLER = "killer";
        public static final String KEY_DEATH_MESSAGE = "deathMessage";
        public static final String KEY_NEW_EXP = "exp";
        public static final String KEY_NEW_LEVEL = "level";
        public static final String KEY_NEW_TOTAL_EXP = "totalExp";
        public static final String KEY_KEEP_LEVEL = "keepLevel";
        public static final String KEY_KEEP_INVENTORY = "keepInventory";
        public static final String KEY_DO_EXP_DROP = "doExpDrop";

        @Nullable
        String killer;
        @Nullable
        String deathMessage;
        int newExp;
        int newLevel;
        int newTotalExp;
        Boolean keepLevel;
        Boolean keepInventory;
        Boolean doExpDrop;

        public DeathArgument(@NotNull String target, @Nullable String killer, @Nullable String deathMessage, int newExp, int newLevel, int newTotalExp, Boolean keepLevel, Boolean keepInventory, Boolean doExpDrop)
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
            if (!(argument instanceof DeathArgument))
                return false;
            DeathArgument a = (DeathArgument) argument;

            return this.checkTargetAndKiller(a)
                    && this.checkDeathMessage(a)
                    && this.checkExpAndLevel(a)
                    && this.checkInventory(a);
        }

        private boolean checkTargetAndKiller(@NotNull DeathArgument argument)
        {
            return this.isSameTarget(argument) &&
                    Objects.equals(this.killer, argument.killer);
        }

        private boolean checkDeathMessage(@NotNull DeathArgument argument)
        {
            return Objects.equals(this.deathMessage, argument.deathMessage);
        }

        private boolean checkExpAndLevel(@NotNull DeathArgument argument)
        {
            return this.newExp == argument.newExp
                    && this.newLevel == argument.newLevel
                    && this.newTotalExp == argument.newTotalExp
                    && this.keepLevel == argument.keepLevel
                    && this.doExpDrop == argument.doExpDrop;
        }

        private boolean checkInventory(@NotNull DeathArgument argument)
        {
            return this.keepInventory == argument.keepInventory;
        }

        @Override
        public String getArgumentString()
        {
            StringBuilder builder = new StringBuilder(super.toString());
            if (this.killer != null)
                builder.append(", killer=").append(this.killer);
            if (this.deathMessage != null)
                builder.append(", deathMessage='").append(this.deathMessage).append('\'');
            if (this.newExp != -1)
                builder.append(", newExp=").append(this.newExp);
            if (this.newLevel != -1)
                builder.append(", newLevel=").append(this.newLevel);
            if (this.newTotalExp != -1)
                builder.append(", newTotalExp=").append(this.newTotalExp);
            if (this.keepLevel != null)
                builder.append(", keepLevel=").append(this.keepLevel);
            if (this.keepInventory != null)
                builder.append(", keepInventory=").append(this.keepInventory);
            if (this.doExpDrop != null)
                builder.append(", doExpDrop=").append(this.doExpDrop);


        }
    }
}
