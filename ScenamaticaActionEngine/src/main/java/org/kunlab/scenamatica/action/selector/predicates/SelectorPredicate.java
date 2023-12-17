package org.kunlab.scenamatica.action.selector.predicates;

import org.bukkit.entity.Entity;

import java.util.Map;

public interface SelectorPredicate<E extends Entity>
{
    boolean test(E entity, Map<? super String, Object> properties);

    default boolean isApplicable(Map<String, Object> properties)
    {
        return true;
    }

    void normalizeMap(Map<? super String, Object> properties);

    String[] getUsingKeys();

    String[] getUsingRangedKeys();

    String[] getUsingAmbiguousStringKeys();

    Class<? extends E> getApplicableClass();

    boolean isApplicableKey(Map<String, Object> properties);
}
