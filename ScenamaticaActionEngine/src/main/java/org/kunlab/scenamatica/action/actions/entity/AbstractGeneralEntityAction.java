package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;

public abstract class AbstractGeneralEntityAction extends AbstractEntityAction<Entity>
{
    public AbstractGeneralEntityAction()
    {
        super(Entity.class, EntityStructure.class);
    }
}
