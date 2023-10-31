package org.kunlab.scenamatica.action.actions.player;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.player.PlayerMoveEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.LocationComparator;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PlayerMoveAction<T extends PlayerMoveAction.Argument> extends AbstractPlayerAction<T>
        implements Executable<T>, Watchable<T>
{
    public static final String KEY_ACTION_NAME = "player_move";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Location toLoc = Utils.assignWorldToLocation(argument.getTo(), engine);
        Player target = argument.getTarget();
        target.teleport(toLoc);
    }

    @Override
    public boolean isFired(@NotNull T argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(argument, engine, event))
            return false;

        assert event instanceof PlayerMoveEvent;
        PlayerMoveEvent e = (PlayerMoveEvent) event;

        return (argument.getFrom() == null || LocationComparator.equals(argument.getFrom(), e.getFrom()))
                && (argument.getTo() == null || LocationComparator.equals(argument.getTo(), e.getTo()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerMoveEvent.class
        );
    }

    @Override
    public T deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        // noinspection unchecked
        return (T) new Argument(
                super.deserializeTarget(map),
                MapUtils.getAsLocationOrNull(map, Argument.KEY_FROM),
                MapUtils.getAsLocationOrNull(map, Argument.KEY_TO)
        );
    }

    @Getter  // Value だと, TeleportAction が継承できない
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_FROM = "from";
        public static final String KEY_TO = "to";

        private final Location from;
        private final Location to;

        public Argument(String target, Location from, Location to)
        {
            super(target);
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && Objects.equals(this.from, arg.from)
                    && Objects.equals(this.to, arg.to);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                ensureNotPresent(KEY_FROM, this.from);
                ensurePresent(KEY_TO, this.to);
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_FROM, this.from,
                    KEY_TO, this.to
            );
        }
    }
}
