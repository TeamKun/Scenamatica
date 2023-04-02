package net.kunmc.lab.scenamatica.action.actions.player;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.scenamatica.action.utils.PlayerUtils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.action.Action;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class PlayerDeathAction implements Action<PlayerDeathAction.DeathArgument>
{
    @Override
    public String getName()
    {
        return "player_death";
    }

    @Override
    public void execute(@Nullable DeathArgument argument)
    {
        if (argument == null)
            throw new IllegalArgumentException("Cannot execute action without argument.");

        Player target = PlayerUtils.getPlayerOrThrow(argument.getTarget());
        String killer = argument.getKiller();
        if (killer != null)
        {
            Player killerPlayer = PlayerUtils.getPlayerOrThrow(killer);
            target.setKiller(killerPlayer);
        }

        target.setHealth(0);
    }

    @Override
    public void onStartWatching(@Nullable DeathArgument argument, @NotNull Plugin plugin, @Nullable Event event)
    {

    }

    @Override
    public boolean isFired(@NotNull DeathArgument argument, @NotNull Plugin plugin, @NotNull Event event)
    {
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
        Player target = PlayerUtils.getPlayerOrThrow(argument.getTarget());
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
        MapUtils.checkType(map, DeathArgument.KEY_TARGET, String.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_KILLER, String.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_DEATH_MESSAGE, String.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_NEW_EXP, Integer.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_NEW_LEVEL, Integer.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_NEW_TOTAL_EXP, Integer.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_KEEP_LEVEL, Boolean.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_KEEP_INVENTORY, Boolean.class);
        MapUtils.checkTypeIfContains(map, DeathArgument.KEY_DO_EXP_DROP, Boolean.class);

        String target = (String) map.get("target");
        String killer = MapUtils.getOrNull(map, "killer");

        String deathMessage = MapUtils.getOrNull(map, "deathMessage");

        int newExp = MapUtils.getOrDefault(map, "exp", -1);
        int newLevel = MapUtils.getOrDefault(map, "level", -1);
        int newTotalExp = MapUtils.getOrDefault(map, "totalExp", -1);
        Boolean keepLevel = MapUtils.getOrNull(map, "keepLevel");
        Boolean keepInventory = MapUtils.getOrNull(map, "keepInventory");
        Boolean doExpDrop = MapUtils.getOrNull(map, "doExpDrop");

        return new DeathArgument(
                target,
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

    @Value
    @AllArgsConstructor
    public static class DeathArgument implements ActionArgument
    {
        public static final String KEY_TARGET = "target";
        public static final String KEY_KILLER = "killer";
        public static final String KEY_DEATH_MESSAGE = "deathMessage";
        public static final String KEY_NEW_EXP = "exp";
        public static final String KEY_NEW_LEVEL = "level";
        public static final String KEY_NEW_TOTAL_EXP = "totalExp";
        public static final String KEY_KEEP_LEVEL = "keepLevel";
        public static final String KEY_KEEP_INVENTORY = "keepInventory";
        public static final String KEY_DO_EXP_DROP = "doExpDrop";

        @NotNull
        String target;
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

        public DeathArgument(@NotNull String target, @Nullable String killer)
        {
            this.target = target;
            this.killer = killer;
            this.deathMessage = null;
            this.newExp = -1;
            this.newLevel = -1;
            this.newTotalExp = -1;
            this.keepLevel = null;
            this.keepInventory = null;
            this.doExpDrop = null;

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
            return this.target.equals(argument.target) && Objects.equals(this.killer, argument.killer);
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

    }
}
