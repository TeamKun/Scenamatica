package org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities;

import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.Creatable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;

/**
 * エンティティのインターフェースです。
 */
public interface AEntityStructure extends EntityStructure, Mapped<Entity>, Creatable<Entity>
{
}
