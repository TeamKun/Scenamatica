package org.kunlab.scenamatica.action.actions.entity;

import io.papermc.paper.event.entity.EntityMoveEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
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

public class EntityMoveAction extends AbstractEntityAction<EntityMoveAction.Argument>
        implements Executable<EntityMoveAction.Argument>, Watchable<EntityMoveAction.Argument>
{
    public static final String KEY_ACTION_NAME = "entity_move";

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

        Entity entity = argument.selectTarget();
        entity.teleport(toLoc);  // TODO: use AI instead of teleport
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(argument, engine, event))
            return false;

        assert event instanceof EntityMoveEvent;
        EntityMoveEvent e = (EntityMoveEvent) event;

        Location fromLoc = argument.getFrom() == null ? null: argument.getFrom().toBlockLocation();
        Location toLoc = argument.getTo() == null ? null: argument.getTo().toBlockLocation();

        Location eFromLoc = e.getFrom().toBlockLocation();
        Location eToLoc = e.getTo().toBlockLocation();

        if (!(fromLoc == null || fromLoc.getWorld() == null || !fromLoc.getWorld().getUID().equals(eFromLoc.getWorld().getUID())))
            return false;
        else if (!(toLoc == null || toLoc.getWorld() == null || !toLoc.getWorld().getUID().equals(eToLoc.getWorld().getUID())))
            return false;

        eFromLoc.setWorld(null);
        eToLoc.setWorld(null);

        return (fromLoc == null || fromLoc.equals(eFromLoc))
                && (toLoc == null || toLoc.equals(eToLoc));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityMoveEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                super.deserializeTarget(map, serializer),
                MapUtils.getAsLocationOrNull(map, Argument.KEY_FROM),
                MapUtils.getAsLocationOrNull(map, Argument.KEY_TO)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractEntityActionArgument
    {
        private static final String KEY_FROM = "from";
        private static final String KEY_TO = "to";

        Location from;
        Location to;

        public Argument(@Nullable Object mayTarget, Location from, Location to)
        {
            super(mayTarget);
            this.from = from;
            this.to = to;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                if (!this.isSelectable())
                    throw new IllegalArgumentException("Cannot select target for this action, please specify target with valid selector.");

                throwIfNotPresent(KEY_TARGET_ENTITY, this.getTargetString());
                throwIfPresent(KEY_FROM, this.from);
                throwIfNotPresent(KEY_TO, this.to);
            }
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
