package org.kunlab.scenamatica.action.actions.base.entity.vehicle;

import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleCreateEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.base.entity.EntitySpawnAction;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.LocationStructure;

import java.util.Collections;
import java.util.List;

@Action("vehicle_create")
@ActionDoc(
        name = "乗り物の生成",
        description = "乗り物を生成します。",
        events = {
                VehicleCreateEvent.class
        },

        executable = "乗り物を生成します。",
        expectable = "乗り物が生成されることを期待します。",
        requireable = ActionDoc.UNALLOWED
)
public class VehicleCreateAction extends AbstractVehicleAction
        implements Executable, Expectable
{
    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        EntityStructure structure = ctxt.input(this.IN_TARGET_ENTITY).getTargetStructure();
        assert structure != null;
        LocationStructure spawnLoc = structure.getLocation();

        if (!isVehicleEntity(structure.getType()))
            throw new IllegalActionInputException(this.IN_TARGET_ENTITY, "Not a vehicle entity provided.");

        EntitySpawnAction.<Vehicle>spawnEntity(ctxt, structure, spawnLoc, e -> this.makeOutputs(ctxt, e));
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedVehicleEvent(ctxt, event))
            return false;

        assert event instanceof VehicleCreateEvent;
        VehicleCreateEvent e = (VehicleCreateEvent) event;

        this.makeOutputs(ctxt, e.getVehicle());

        return true;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(VehicleCreateEvent.class);
    }
}
