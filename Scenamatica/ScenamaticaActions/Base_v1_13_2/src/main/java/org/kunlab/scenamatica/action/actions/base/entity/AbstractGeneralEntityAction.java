package org.kunlab.scenamatica.action.actions.base.entity;

import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;

public abstract class AbstractGeneralEntityAction extends AbstractEntityAction<Entity, EntityStructure>
{
    public AbstractGeneralEntityAction()
    {
        super(Entity.class, EntityStructure.class);
    }
}
