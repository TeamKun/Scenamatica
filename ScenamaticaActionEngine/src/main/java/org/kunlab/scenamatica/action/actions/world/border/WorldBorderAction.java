package org.kunlab.scenamatica.action.actions.world.border;

import io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent;
import io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent;
import org.bukkit.Location;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.world.AbstractWorldAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;

import java.util.Arrays;
import java.util.List;

public class WorldBorderAction extends AbstractWorldAction
        implements Executable, Watchable, Requireable
{
    // WorldBorderBoundsChangeEvent と WorldBorderCenterChangeEvent を処理する

    public static final String KEY_ACTION_NAME = "world_border";

    public static final InputToken<WorldBorderBoundsChangeEvent.Type> IN_TYPE = ofEnumInput(
            "type",
            WorldBorderBoundsChangeEvent.Type.class
    );
    public static final InputToken<Double> IN_SIZE = ofInput(
            "size",
            Double.class
    );
    public static final InputToken<Double> IN_SIZE_OLD = ofInput(
            "sizeOld",
            Double.class
    );
    public static final InputToken<Long> IN_DURATION = ofInput(
            "duration",
            Long.class
    );
    public static final InputToken<LocationStructure> IN_CENTER = ofInput(
            "center",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );
    public static final InputToken<LocationStructure> IN_CENTER_OLD = ofInput(
            "centerOld",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        WorldBorder border = super.getWorldNonNull(argument, engine).getWorldBorder();

        argument.runIfPresent(IN_SIZE, border::setSize);
        argument.runIfPresent(IN_CENTER, center -> border.setCenter(center.create()));
    }

    @Override
    public boolean isFired(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
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

        return argument.ifPresent(IN_TYPE, inType -> inType == type)
                && argument.ifPresent(IN_SIZE, inSize -> inSize == size)
                && argument.ifPresent(IN_SIZE_OLD, inSizeOld -> inSizeOld == sizeOld)
                && argument.ifPresent(IN_DURATION, inDuration -> inDuration == duration)
                && argument.ifPresent(IN_CENTER, inCenter -> inCenter.isAdequate(center))
                && argument.ifPresent(IN_CENTER_OLD, inCenterOld -> inCenterOld.isAdequate(centerOld));
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
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        WorldBorder border = super.getWorldNonNull(argument, engine).getWorldBorder();

        return argument.ifPresent(IN_SIZE, size -> size == border.getSize())
                && argument.ifPresent(IN_DURATION, duration -> duration == border.getWarningDistance())
                && argument.ifPresent(IN_CENTER, center -> center.isAdequate(border.getCenter()));
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_SIZE, IN_DURATION, IN_CENTER);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.registerAll(IN_TYPE, IN_SIZE_OLD, IN_CENTER_OLD);
        return board;
    }
}
