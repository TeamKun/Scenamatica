package org.kunlab.scenamatica.interfaces.scenariofile.entity;

import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.interfaces.scenariofile.Creatable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;

/**
 * エンティティのインターフェースです。
 */
public interface EntityStructure extends GenericEntityStructure, Mapped<Entity>, Creatable<Entity>
{
}
