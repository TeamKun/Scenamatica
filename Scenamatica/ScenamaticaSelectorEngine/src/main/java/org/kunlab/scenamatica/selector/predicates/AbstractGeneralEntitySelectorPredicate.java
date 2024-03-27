package org.kunlab.scenamatica.selector.predicates;

import org.bukkit.entity.Entity;

public abstract class AbstractGeneralEntitySelectorPredicate extends AbstractSelectorPredicate<Entity>
{
    @Override
    public Class<? extends Entity> getApplicableClass()
    {
        return Entity.class;
    }
}
