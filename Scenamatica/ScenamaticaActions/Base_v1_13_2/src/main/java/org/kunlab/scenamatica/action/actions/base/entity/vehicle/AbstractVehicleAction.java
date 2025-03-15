package org.kunlab.scenamatica.action.actions.base.entity.vehicle;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Event;
import org.bukkit.event.vehicle.VehicleEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.base.entity.AbstractEntityAction;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.VehicleStructure;

public abstract class AbstractVehicleAction extends AbstractEntityAction<Vehicle, VehicleStructure>
{
    public AbstractVehicleAction()
    {
        super(Vehicle.class, VehicleStructure.class);
    }

    public static boolean isVehicleEntity(@NotNull Class<? extends Entity> entity)
    {
        return Vehicle.class.isAssignableFrom(entity);
    }

    public static boolean isVehicleEntity(@NotNull EntityType type)
    {
        if (type == EntityType.UNKNOWN)
            return false;
        assert type.getEntityClass() != null;  // EntityType.UNKNOWN のときのみ null なので。

        return isVehicleEntity(type.getEntityClass());
    }

    protected boolean checkMatchedVehicleEvent(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof VehicleEvent))
            return false;

        VehicleEvent e = (VehicleEvent) event;
        return ctxt.ifHasInput(this.IN_TARGET_ENTITY, specifier -> specifier.checkMatchedEntity(e.getVehicle()));
    }
}
