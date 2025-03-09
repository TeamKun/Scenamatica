package org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities;

import org.bukkit.entity.Vehicle;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;

@TypeDoc(
        name = "Vehicle",
        description = "乗り物エンティティの情報を格納します。",
        mappingOf = Vehicle.class
)
public interface VehicleStructure extends EntityStructure
{

}
