package org.kunlab.scenamatica.action.selector.predicates;

import org.bukkit.entity.Player;

public abstract class AbstractPlayerSelectorPredicate extends AbstractSelectorPredicate<Player>
{
    @Override
    public Class<? extends Player> getApplicableClass()
    {
        return Player.class;
    }
}
