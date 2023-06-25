package org.kunlab.scenamatica.action.actions.world.border;

import io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent;
import io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.Requireable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.world.AbstractWorldAction;
import org.kunlab.scenamatica.action.actions.world.AbstractWorldActionArgument;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WorldBorderAction extends AbstractWorldAction<WorldBorderAction.Argument> implements Requireable<WorldBorderAction.Argument>
{
    // WorldBorderBoundsChangeEvent と WorldBorderCenterChangeEvent を処理する

    public static final String KEY_ACTION_NAME = "world_border";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        WorldBorder border = argument.getWorldNonNull(engine).getWorldBorder();

        if (argument.getSize() != -1)
            border.setSize(argument.getSize(), argument.getDuration());
        if (argument.getCenter() != null)
            border.setCenter(argument.getCenter());
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        switch (type)
        {
            case ACTION_EXECUTE:
            case CONDITION_REQUIRE:
                this.throwIfPresent(Argument.KEY_TYPE, argument.getType());
                this.throwIfPresent(Argument.KEY_SIZE_OLD, argument.getOldSize());
                this.throwIfPresent(Argument.KEY_CENTER_OLD, argument.getOldCenter());
                break;
        }
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.isFired(argument, engine, event))
            return false;

        WorldBorderBoundsChangeEvent.Type type;
        double size;
        double sizeOld;
        long duration;
        Location center;
        Location centerOld;

        if (event instanceof WorldBorderBoundsChangeEvent)
        {
            WorldBorderBoundsChangeEvent e = (WorldBorderBoundsChangeEvent) event;
            type = e.getType();
            size = e.getNewSize();
            sizeOld = e.getOldSize();
            duration = e.getDuration();
            center = e.getWorldBorder().getCenter();
            centerOld = null;
        }
        else /* assert event instanceof WorldBorderCenterChangeEvent */
        {
            WorldBorderCenterChangeEvent e = (WorldBorderCenterChangeEvent) event;
            WorldBorder border = e.getWorldBorder();
            type = null;
            size = border.getSize();
            sizeOld = border.getSize();
            duration = 0;
            center = e.getNewCenter();
            centerOld = e.getOldCenter();
        }

        return (argument.getType() == null || argument.getType() == type)
                && (argument.getSize() == -1 || argument.getSize() == size)
                && (argument.getOldSize() == -1 || argument.getOldSize() == sizeOld)
                && (argument.getDuration() == -1 || argument.getDuration() == duration)
                && (argument.getCenter() == null || Objects.equals(argument.getCenter(), center))
                && (argument.getOldCenter() == null || Objects.equals(argument.getOldCenter(), centerOld));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Arrays.asList(
                WorldBorderBoundsChangeEvent.class,
                WorldBorderCenterChangeEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        MapUtils.checkEnumNameIfContains(map, Argument.KEY_TYPE, WorldBorderBoundsChangeEvent.Type.class);
        MapUtils.checkNumberIfContains(map, Argument.KEY_SIZE);
        MapUtils.checkNumberIfContains(map, Argument.KEY_SIZE_OLD);
        MapUtils.checkNumberIfContains(map, Argument.KEY_DURATION);

        MapUtils.checkLocationIfContains(map, Argument.KEY_CENTER);
        MapUtils.checkLocationIfContains(map, Argument.KEY_CENTER_OLD);

        return new Argument(
                super.deserializeWorld(map),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_TYPE, WorldBorderBoundsChangeEvent.Type.class),
                MapUtils.getAsNumberSafe(map, Argument.KEY_SIZE).doubleValue(),
                MapUtils.getAsNumberSafe(map, Argument.KEY_SIZE_OLD).doubleValue(),
                MapUtils.getAsNumberSafe(map, Argument.KEY_DURATION).longValue(),
                MapUtils.getAsLocationOrNull(map, Argument.KEY_CENTER),
                MapUtils.getAsLocationOrNull(map, Argument.KEY_CENTER_OLD)
        );
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = this.requireArgsNonNull(argument);

        WorldBorder border = argument.getWorldNonNull(engine).getWorldBorder();

        return (argument.getSize() == -1 || argument.getSize() == border.getSize())
                && (argument.getDuration() == -1 || argument.getDuration() == border.getSize())
                && (argument.getCenter() == null || Objects.equals(argument.getCenter(), border.getCenter()));
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractWorldActionArgument
    {
        public static final String KEY_TYPE = "type";
        public static final String KEY_SIZE = "size";
        public static final String KEY_SIZE_OLD = "size_old";
        public static final String KEY_DURATION = "duration";

        public static final String KEY_CENTER = "center";
        public static final String KEY_CENTER_OLD = "center_old";

        @Nullable
        WorldBorderBoundsChangeEvent.Type type;
        double size;
        double oldSize;
        long duration;

        @Nullable
        Location center;
        @Nullable
        Location oldCenter;

        public Argument(@NotNull NamespacedKey worldRef, @Nullable WorldBorderBoundsChangeEvent.Type type, double size, double oldSize, long duration, @Nullable Location center, @Nullable Location oldCenter)
        {
            super(worldRef);
            this.type = type;
            this.size = size;
            this.oldSize = oldSize;
            this.duration = duration;
            this.center = center;
            this.oldCenter = oldCenter;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return super.isSame(arg)
                    && Objects.equals(this.center, arg.center)
                    && this.type == arg.type
                    && this.size == arg.size
                    && this.oldSize == arg.oldSize
                    && this.duration == arg.duration;
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_TYPE, this.type,
                    KEY_SIZE, this.size,
                    KEY_SIZE_OLD, this.oldSize,
                    KEY_DURATION, this.duration,
                    KEY_CENTER, this.center,
                    KEY_CENTER_OLD, this.oldCenter
            );
        }
    }
}
