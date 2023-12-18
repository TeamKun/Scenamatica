package org.kunlab.scenamatica.action.selector.predicates;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.action.selector.compiler.AmbiguousString;
import org.kunlab.scenamatica.action.selector.compiler.NegateSupport;
import org.kunlab.scenamatica.action.selector.compiler.parser.NegativeValue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TagPredicate extends AbstractGeneralEntitySelectorPredicate
{
    public static final String KEY_TAG = "tag";
    public static final String KEY_TAG_2 = "tags";

    private static Object mergeTags(Object toList, Object element)
    {
        boolean negated = NegateSupport.isNegative(toList);
        List<Object> list;
        if (toList == null)
            list = new ArrayList<>();
        else
        {
            if (toList instanceof List)
                list = NegateSupport.toRawCast(toList);
            else
            {
                list = new ArrayList<>();
                list.add(toList);
            }
        }

        if (element instanceof List)
            list.addAll(NegateSupport.toRawCast(element));
        else
            list.add(element);

        return negated ? new NegativeValue(list): list;
    }

    private static boolean hasTag(Entity entity, AmbiguousString hasTag)
    {
        for (String tag : entity.getScoreboardTags())
            if (hasTag.test(tag))
                return true;

        return false;
    }

    @Override
    public boolean test(Player basis, Entity entity, Map<? super String, Object> properties)
    {
        Object rawTag = NegateSupport.toRawCast(properties.get(KEY_TAG));

        if (rawTag instanceof List)
        {
            boolean negated = NegateSupport.isNegative(properties.get(KEY_TAG));
            // noinspection unchecked
            List<Object> list = (List<Object>) rawTag;
            for (Object obj : list)
            {
                AmbiguousString ambiguousString = AmbiguousString.fromObject(obj);
                if (!hasTag(entity, ambiguousString))  // Negate は考慮されている
                    return negated;
            }

            return !negated;
        }
        else
        {
            AmbiguousString ambiguousString = (AmbiguousString) rawTag;
            return hasTag(entity, ambiguousString);
        }
    }

    @Override
    public void normalizeMap(Map<? super String, Object> properties)
    {
        if (properties.containsKey(KEY_TAG_2))
            properties.put(KEY_TAG, mergeTags(properties.get(KEY_TAG), properties.get(KEY_TAG_2)));

        if (!properties.containsKey(KEY_TAG))
            return;

        Object rawTag = NegateSupport.toRawCast(properties.get(KEY_TAG));
        boolean negated = NegateSupport.isNegative(rawTag);

        if (rawTag instanceof List)
        {
            List<Object> list = NegateSupport.toRawCast(rawTag);
            AmbiguousString.normalizeList(list);
            properties.put(KEY_TAG, negated ? new NegativeValue(list): list);
        }
        else
            AmbiguousString.normalizeMap(KEY_TAG, KEY_TAG, properties);
    }

    @Override
    public String[] getUsingAmbiguousStringKeys()
    {
        return new String[]{
                KEY_TAG,
                KEY_TAG_2
        };
    }

    @Override
    public String[] getUsingKeys()
    {
        return new String[]{
                KEY_TAG,
                KEY_TAG_2
        };
    }
}
