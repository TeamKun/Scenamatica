package org.kunlab.scenamatica.selector.predicates;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Map;

public interface SelectorPredicate<E extends Entity>
{
    boolean test(Player basis, E entity, Map<? super String, Object> properties);

    default boolean isApplicable(Map<String, Object> properties)
    {
        return true;
    }

    void normalizeMap(Map<? super String, Object> properties);

    String[] getUsingKeys();

    String[] getUsingRangedKeys();

    String[] getUsingAmbiguousStringKeys();

    Class<? extends E> getApplicableClass();

    boolean isBasisRequired();

    boolean isApplicableKey(Map<String, Object> properties);
}
