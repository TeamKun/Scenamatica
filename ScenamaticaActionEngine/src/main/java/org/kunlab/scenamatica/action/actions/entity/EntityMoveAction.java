package org.kunlab.scenamatica.action.actions.entity;

import io.papermc.paper.event.entity.EntityMoveEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.LocationComparator;
import org.kunlab.scenamatica.commons.specifiers.EntitySpecifierImpl;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
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
        Entity entity = argument.selectTarget(engine.getContext());

        if (argument.isUseAI() && entity instanceof Mob)
        {
            Mob mob = (Mob) entity;
            boolean success = mob.getPathfinder().moveTo(toLoc);
            if (!success)
                throw new IllegalStateException("Failed to find path from " + entity.getLocation() + " to " + toLoc);
        }
        else
            entity.teleport(toLoc);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(argument, engine, event))
            return false;

        assert event instanceof EntityMoveEvent;
        EntityMoveEvent e = (EntityMoveEvent) event;

        return (argument.getFrom() == null || LocationComparator.equals(argument.getFrom(), e.getFrom()))
                && (argument.getTo() == null || LocationComparator.equals(argument.getTo(), e.getTo()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityMoveEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new Argument(
                super.deserializeTarget(map, serializer),
                MapUtils.getAsLocationOrNull(map, Argument.KEY_FROM),
                MapUtils.getAsLocationOrNull(map, Argument.KEY_TO),
                MapUtils.getOrDefault(map, Argument.KEY_USE_AI, true)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractEntityActionArgument<Entity>
    {
        public static final String KEY_FROM = "from";
        public static final String KEY_TO = "to";
        public static final String KEY_USE_AI = "ai";

        Location from;
        Location to;
        // Execute のときのみ. デフォは true -> テレポート.
        boolean useAI;

        public Argument(@Nullable EntitySpecifierImpl<Entity> mayTarget, Location from, Location to, boolean useAI)
        {
            super(mayTarget);
            this.from = from;
            this.to = to;
            this.useAI = useAI;
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                this.throwIfNotSelectable();
                ensurePresent(KEY_TARGET_ENTITY, this.getTargetString());
                ensureNotPresent(KEY_FROM, this.from);
                ensurePresent(KEY_TO, this.to);
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
