package org.kunlab.scenamatica.action.actions.extend_v1_16_5.world.border;

import io.papermc.paper.event.world.border.WorldBorderBoundsChangeEvent;
import io.papermc.paper.event.world.border.WorldBorderCenterChangeEvent;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.base.world.AbstractWorldAction;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.Creatable;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;

import java.util.Arrays;
import java.util.List;

@ActionMeta(value = "world_border", supportsSince = MinecraftVersion.V1_16_5)
public class WorldBorderAction extends AbstractWorldAction
        implements Executable, Watchable, Requireable
{
    // WorldBorderBoundsChangeEvent と WorldBorderCenterChangeEvent を処理する

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

    public static final String KEY_TYPE = "type";
    public static final String KEY_SIZE = "size";
    public static final String KEY_SIZE_OLD = "sizeOld";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_CENTER = "center";
    public static final String KEY_CENTER_OLD = "centerOld";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        World world = super.getWorldNonNull(ctxt);
        WorldBorder border = world.getWorldBorder();
        this.makeOutputs(
                ctxt,
                world,
                null,
                ctxt.orElseInput(IN_SIZE, () -> null),
                border.getSize(),
                ctxt.orElseInput(IN_DURATION, () -> null),
                ctxt.ifHasInput(IN_CENTER, Creatable::create, null),
                border.getCenter()
        );

        ctxt.runIfHasInput(IN_SIZE, size -> border.setSize(size, ctxt.orElseInput(IN_DURATION, () -> 0L)));
        ctxt.runIfHasInput(IN_CENTER, center -> border.setCenter(center.create()));
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkFired(ctxt, event))
            return false;

        WorldBorderBoundsChangeEvent.Type type;
        World world = ((WorldEvent) event).getWorld();
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

        boolean result = ctxt.ifHasInput(IN_TYPE, inType -> inType == type)
                && ctxt.ifHasInput(IN_SIZE, inSize -> inSize == size)
                && ctxt.ifHasInput(IN_SIZE_OLD, inSizeOld -> inSizeOld == sizeOld)
                && ctxt.ifHasInput(IN_DURATION, inDuration -> inDuration == duration)
                && ctxt.ifHasInput(IN_CENTER, inCenter -> inCenter.isAdequate(center))
                && ctxt.ifHasInput(IN_CENTER_OLD, inCenterOld -> inCenterOld.isAdequate(centerOld));
        if (result)
            this.makeOutputs(ctxt, world, type, size, sizeOld, duration, center, centerOld);

        return result;
    }

    private void makeOutputs(@NotNull ActionContext ctxt, @NotNull World world, @Nullable WorldBorderBoundsChangeEvent.Type type,
                             Double size, Double sizeOld, Long duration, Location center, Location centerOld)
    {
        if (type != null)
            ctxt.output(KEY_TYPE, type);
        if (size != null)
            ctxt.output(KEY_SIZE, size);
        if (sizeOld != null)
            ctxt.output(KEY_SIZE_OLD, sizeOld);
        if (duration != null)
            ctxt.output(KEY_DURATION, duration);
        if (center != null)
            ctxt.output(KEY_CENTER, center);
        if (centerOld != null)
            ctxt.output(KEY_CENTER_OLD, centerOld);
        super.makeOutputs(ctxt, world);
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
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        WorldBorder border = super.getWorldNonNull(ctxt).getWorldBorder();

        return ctxt.ifHasInput(IN_SIZE, size -> size == border.getSize())
                && ctxt.ifHasInput(IN_DURATION, duration -> duration == border.getWarningDistance())
                && ctxt.ifHasInput(IN_CENTER, center -> center.isAdequate(border.getCenter()));
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
