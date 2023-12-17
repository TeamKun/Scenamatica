package org.kunlab.scenamatica.action.selector.predicates;

import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.action.selector.compiler.AmbiguousString;
import org.kunlab.scenamatica.action.selector.compiler.RangedNumber;
import org.kunlab.scenamatica.commons.utils.MapUtils;

import java.util.Map;
import java.util.Optional;

public abstract class AbstractSelectorPredicate<E extends Entity> implements SelectorPredicate<E>
{
    protected static boolean isInRangeOrUnspecified(String key, Map<String, Object> properties)
    {
        Optional<Number> number = MapUtils.getAsNumber(properties, key);
        if (!number.isPresent())
            return true;

        RangedNumber range = RangedNumber.fromMap(key, properties);
        return range.test(number.get());
    }

    protected static String normalizeString(Object obj)
    {
        return obj == null ? null: obj.toString();
    }

    @Override
    public void normalizeMap(Map<? super String, Object> properties)
    {

    }

    @Override
    public String[] getUsingRangedKeys()
    {
        return new String[0];
    }

    @Override
    public String[] getUsingAmbiguousStringKeys()
    {
        return new String[0];
    }

    @Override
    public boolean isApplicableKey(Map<String, Object> properties)

    {
        for (String key : this.getUsingKeys())
            if (properties.containsKey(key))
                return this.isApplicable(properties);

        for (String key : this.getUsingRangedKeys())
            if (RangedNumber.hasRangedNumber(key, properties))
                return this.isApplicable(properties);
        for (String key : this.getUsingAmbiguousStringKeys())
            if (AmbiguousString.hasAmbiguousString(key, properties))
                return this.isApplicable(properties);

        return false;
    }
}
