package org.kunlab.scenamatica.action.actions.extended_v1_14.world.weather;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.kunlab.scenamatica.bookkeeper.enums.MCVersion;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.LightningStrikeStructure;
import org.kunlab.scenamatica.nms.NMSProvider;
import org.kunlab.scenamatica.nms.enums.entity.NMSLightningStrikeCause;
import org.kunlab.scenamatica.nms.types.world.NMSWorldServer;

import java.util.Collections;
import java.util.List;

// `cause` の追加

@Action(value = "weather_strike_lightning", supportsSince = MinecraftVersion.V1_14)
@OutputDocs({
        @OutputDoc(
                name = WeatherStrikeLightningAction.OUT_CAUSE,
                description = "落雷の原因です。",
                type = LightningStrikeEvent.Cause.class
        )
})
public class WeatherStrikeLightningAction extends org.kunlab.scenamatica.action.actions.base.world.weather.WeatherStrikeLightningAction
        implements Executable, Expectable
{
    public static final String OUT_CAUSE = "cause";

    @InputDoc(
            name = "cause",
            description = "落雷の原因です。",
            type = LightningStrikeEvent.Cause.class,
            supportsSince = MCVersion.V1_14
    )
    public static final InputToken<LightningStrikeEvent.Cause> IN_CAUSE = ofInput(
            "cause",
            LightningStrikeEvent.Cause.class,
            ofEnum(LightningStrikeEvent.Cause.class)
    );

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        // `cause` の追加
        LightningStrikeEvent.Cause cause = ctxt.orElseInput(IN_CAUSE, () -> LightningStrikeEvent.Cause.UNKNOWN);

        LightningStrikeStructure lightning = ctxt.input(IN_LIGHTNING);
        boolean isEffect = Boolean.TRUE.equals(lightning.isEffect());
        Location location = super.retrieveLocation(ctxt, lightning);

        this.makeOutputs(ctxt, location.getWorld(), lightning, cause);
        NMSWorldServer world = NMSProvider.getProvider().wrap(location.getWorld());
        world.strikeLightning(location, isEffect, NMSLightningStrikeCause.fromBukkit(cause));
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof LightningStrikeEvent))
            return false;
        LightningStrikeEvent e = (LightningStrikeEvent) event;

        // `cause` の追加
        return ctxt.ifHasInput(IN_CAUSE, cause -> cause == e.getCause())
                && super.checkFired(ctxt, e);
    }

    @Override
    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull LightningStrikeEvent event)
    {
        // `cause` の追加
        ctxt.output(OUT_CAUSE, event.getCause());
        super.makeOutputs(ctxt, event);
    }

    @Override
    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull World world, @NotNull LightningStrikeStructure lightning, @Nullable LightningStrikeEvent.Cause cause)
    {
        // `cause` の追加
        if (cause != null)
            ctxt.output(OUT_CAUSE, cause);
        super.makeOutputs(ctxt, world, lightning, cause);
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return super.getInputBoard(type)
                .register(IN_CAUSE);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(LightningStrikeEvent.class);
    }
}
