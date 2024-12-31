package org.kunlab.scenamatica.action.actions.base.world.weather;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.LightningStrikeStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;

import java.util.Collections;
import java.util.List;

@Action(value = "weather_strike_lightning", supportsUntil = MinecraftVersion.V1_13_2)
@ActionDoc(
        name = "落雷",
        description = "ワールドに落雷を発生させます。",

        executable = "ワールドに落雷を発生させます。",
        expectable = "ワールドに落雷が発生するまで待機します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = WeatherStrikeLightningAction.OUT_LIGHTNING,
                        description = "落雷のエンティティです。",
                        type = LightningStrikeStructure.class
                )
        }
)
public class WeatherStrikeLightningAction extends AbstractWeatherAction
        implements Executable, Expectable
{
    public static final String OUT_LIGHTNING = "lightning";

    @InputDoc(
            name = "lightning",
            description = "落雷のエンティティです。",
            type = LightningStrikeStructure.class
    )
    public static final InputToken<LightningStrikeStructure> IN_LIGHTNING = ofInput(
            "lightning",
            LightningStrikeStructure.class,
            ofDeserializer(LightningStrikeStructure.class)
    );

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        LightningStrikeStructure lightning = ctxt.input(IN_LIGHTNING);
        Location location = this.retrieveLocation(ctxt, lightning);

        if (Boolean.TRUE.equals(lightning.isEffect()))
            location.getWorld().strikeLightningEffect(location);
        else
            location.getWorld().strikeLightning(location);
    }

    protected Location retrieveLocation(@NotNull ActionContext ctxt, @NotNull LightningStrikeStructure lightning)
    {
        LocationStructure locationStructure = lightning.getLocation();
        if (locationStructure == null
                || locationStructure.getX() == null || locationStructure.getY() == null || locationStructure.getZ() == null)
            throw new IllegalActionInputException(IN_LIGHTNING, "Location(x, y, z) is not set.");

        Location location;
        // 引数に直接ワールドが指定されている場合は, それを優先
        if (ctxt.hasInput(IN_WORLD) || ctxt.getContext().hasStage())
        {
            World world = super.getWorldNonNull(ctxt);
            location = locationStructure.create(world);
        }
        else if (locationStructure.getWorld() != null) // それ以外の場合は, Location に設定されているワールドを使用
            location = locationStructure.create();
        else
            throw new IllegalActionInputException(IN_LIGHTNING, "World is not set.");

        return location;
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof LightningStrikeEvent))
            return false;
        LightningStrikeEvent e = (LightningStrikeEvent) event;

        boolean result = ctxt.ifHasInput(IN_LIGHTNING, lightning -> lightning.isAdequate(e.getLightning()));
        if (result)
            this.makeOutputs(ctxt, e);

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull LightningStrikeEvent event)
    {
        ctxt.output(OUT_LIGHTNING, event.getLightning());
        super.makeOutputs(ctxt, event.getLightning().getWorld());
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull World world, @NotNull LightningStrikeStructure lightning, @Nullable LightningStrikeEvent.Cause cause)
    {
        ctxt.output(OUT_LIGHTNING, lightning);
        super.makeOutputs(ctxt, world);
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return super.getInputBoard(type)
                .register(IN_LIGHTNING)
                .requirePresent(IN_LIGHTNING);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(LightningStrikeEvent.class);
    }
}
