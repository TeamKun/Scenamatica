package org.kunlab.scenamatica.interfaces.scenariofile.entity.entities;

import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.interfaces.scenariofile.Creatable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;

/**
 * エンティティのインターフェースです。
 */
public interface AEntityStructure extends EntityStructure, Mapped<Entity>, Creatable<Entity>
{
}
