package org.kunlab.scenamatica.action.actions.base.entity;

import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.AEntityStructure;

public abstract class AbstractGeneralEntityAction extends AbstractEntityAction<Entity, AEntityStructure>
{
    public AbstractGeneralEntityAction()
    {
        super(Entity.class, AEntityStructure.class);
    }
}
