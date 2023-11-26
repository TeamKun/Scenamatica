package org.kunlab.scenamatica.action.actions.player;

import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class PlayerRespawnAction extends AbstractPlayerAction<PlayerRespawnAction.Argument>
        implements Executable<PlayerRespawnAction.Argument>, Watchable<PlayerRespawnAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_respawn";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player player = argument.getTarget();
        if (!player.isDead())
            throw new IllegalStateException("Player is not dead");

        if (argument.getLocation() != null)
            player.setBedSpawnLocation(Utils.assignWorldToLocation(argument.getLocation(), engine), true);

        player.spigot().respawn();
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof PlayerRespawnEvent || super.checkMatchedPlayerEvent(argument, engine, event)))
            return false;

        if (event instanceof PlayerRespawnEvent)
        {
            PlayerRespawnEvent e = (PlayerRespawnEvent) event;

            return (argument.getIsBed() == null || argument.getIsBed() == e.isBedSpawn())
                    && (argument.getIsAnchor() == null || argument.getIsAnchor() == e.isAnchorSpawn());
        }
        else
        {
            assert event instanceof PlayerPostRespawnEvent;
            PlayerPostRespawnEvent e = (PlayerPostRespawnEvent) event;

            return (argument.getIsBed() == null || argument.getIsBed() == e.isBedSpawn())
                    && (argument.getLocation() == null || argument.getLocation().equals(e.getRespawnedLocation()));
        }
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Arrays.asList(
                PlayerRespawnEvent.class,
                PlayerPostRespawnEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new Argument(
                super.deserializeTarget(map),
                MapUtils.getOrNull(map, Argument.KEY_IS_BED),
                MapUtils.getOrNull(map, Argument.KEY_IS_ANCHOR),
                MapUtils.getAsLocationOrNull(map, Argument.KEY_LOCATION)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_IS_BED = "isBed";
        public static final String KEY_IS_ANCHOR = "isAnchor";
        public static final String KEY_LOCATION = "location";

        Boolean isBed;
        Boolean isAnchor;
        Location location;

        public Argument(String target, Boolean isBed, Boolean isAnchor, Location location)
        {
            super(target);
            this.isBed = isBed;
            this.isAnchor = isAnchor;
            this.location = location;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;
            return super.isSame(arg)
                    && this.isBed == arg.isBed
                    && this.isAnchor == arg.isAnchor;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type != ScenarioType.ACTION_EXECUTE)
                return;

            ensureNotPresent(Argument.KEY_IS_BED, this.isBed);
            ensureNotPresent(Argument.KEY_IS_ANCHOR, this.isAnchor);
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_IS_BED, this.isBed,
                    KEY_IS_ANCHOR, this.isAnchor
            );
        }
    }
}
