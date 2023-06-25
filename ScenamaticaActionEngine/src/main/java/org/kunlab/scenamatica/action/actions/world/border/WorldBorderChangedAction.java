package org.kunlab.scenamatica.action.actions.world.border;

import io.papermc.paper.event.world.border.WorldBorderBoundsChangeFinishEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.NamespacedKey;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.world.AbstractWorldAction;
import org.kunlab.scenamatica.action.actions.world.AbstractWorldActionArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WorldBorderChangedAction extends AbstractWorldAction<WorldBorderChangedAction.Argument>
{
    public static final String KEY_ACTION_NAME = "world_border_changed";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.isFired(argument, engine, event))
            return false;

        assert event instanceof WorldBorderBoundsChangeFinishEvent;
        WorldBorderBoundsChangeFinishEvent e = (WorldBorderBoundsChangeFinishEvent) event;

        return (argument.getSize() == -1 || argument.getSize() == e.getNewSize())
                && (argument.getOldSize() == -1 || argument.getOldSize() == e.getOldSize())
                && (argument.getDuration() == -1 || argument.getDuration() == e.getDuration());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WorldBorderBoundsChangeFinishEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        MapUtils.checkNumberIfContains(map, WorldBorderAction.Argument.KEY_SIZE);
        MapUtils.checkNumberIfContains(map, WorldBorderAction.Argument.KEY_SIZE_OLD);
        MapUtils.checkNumberIfContains(map, WorldBorderAction.Argument.KEY_DURATION);

        return new WorldBorderChangedAction.Argument(
                super.deserializeWorld(map),
                MapUtils.getAsNumberSafe(map, WorldBorderAction.Argument.KEY_SIZE).doubleValue(),
                MapUtils.getAsNumberSafe(map, WorldBorderAction.Argument.KEY_SIZE_OLD).doubleValue(),
                MapUtils.getAsNumberSafe(map, WorldBorderAction.Argument.KEY_DURATION).longValue()
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractWorldActionArgument
    {
        public static final String KEY_SIZE = "size";
        public static final String KEY_SIZE_OLD = "size_old";
        public static final String KEY_DURATION = "duration";

        double size;
        double oldSize;
        long duration;

        public Argument(@Nullable NamespacedKey worldRef, double size, double oldSize, long duration)
        {
            super(worldRef);
            this.size = size;
            this.oldSize = oldSize;
            this.duration = duration;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof WorldBorderChangedAction.Argument))
                return false;

            WorldBorderChangedAction.Argument arg = (WorldBorderChangedAction.Argument) argument;

            return super.isSame(arg)
                    && this.size == arg.size
                    && this.oldSize == arg.oldSize
                    && this.duration == arg.duration;
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_SIZE, this.size,
                    KEY_SIZE_OLD, this.oldSize,
                    KEY_DURATION, this.duration
            );
        }
    }
}
