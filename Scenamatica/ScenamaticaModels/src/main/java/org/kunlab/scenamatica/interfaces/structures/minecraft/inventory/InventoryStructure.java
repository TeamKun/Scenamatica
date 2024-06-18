package org.kunlab.scenamatica.interfaces.structures.minecraft.inventory;

import org.bukkit.inventory.Inventory;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.interfaces.scenariofile.Creatable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;

/**
 * インベントリの構造体の既定のインターフェースです。
 */
@TypeDoc(
        name = "Inventory",
        description = "インベントリの情報を格納します。",
        mappingOf = Inventory.class
)
public interface InventoryStructure extends GenericInventoryStructure, Mapped<Inventory>, Creatable<Inventory>
{
}
