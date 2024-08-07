package org.kunlab.scenamatica.action.actions.extend_v1_16_5.world.border;

import io.papermc.paper.event.world.border.WorldBorderBoundsChangeFinishEvent;
import org.bukkit.event.Event;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.base.world.AbstractWorldAction;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

import java.util.Collections;
import java.util.List;

@Action(value = "world_border_changed", supportsSince = MinecraftVersion.V1_16_5)
public class WorldBorderChangedAction extends AbstractWorldAction
        implements Expectable
{
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
    public static final String KEY_OUT_SIZE = "size";
    public static final String KEY_OUT_SIZE_OLD = "sizeOld";
    public static final String KEY_OUT_DURATION = "duration";

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkFired(ctxt, event))
            return false;

        assert event instanceof WorldBorderBoundsChangeFinishEvent;
        WorldBorderBoundsChangeFinishEvent e = (WorldBorderBoundsChangeFinishEvent) event;

        boolean result = ctxt.ifHasInput(IN_SIZE, size -> size == e.getNewSize())
                && ctxt.ifHasInput(IN_SIZE_OLD, sizeOld -> sizeOld == e.getOldSize())
                && ctxt.ifHasInput(IN_DURATION, duration -> duration == e.getDuration());
        if (result)
            this.makeOutputs(ctxt, e);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull WorldBorderBoundsChangeFinishEvent event)
    {
        ctxt.output(KEY_OUT_SIZE, event.getNewSize());
        ctxt.output(KEY_OUT_SIZE_OLD, event.getOldSize());
        ctxt.output(KEY_OUT_DURATION, event.getDuration());
        super.makeOutputs(ctxt, event.getWorld());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WorldBorderBoundsChangeFinishEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {

        return super.getInputBoard(type)
                .registerAll(IN_SIZE, IN_SIZE_OLD, IN_DURATION);
    }
}
