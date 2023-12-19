package org.kunlab.scenamatica.action.selector.predicates;

import org.bukkit.entity.Player;
import org.kunlab.scenamatica.action.selector.compiler.RangedNumber;

import java.util.Map;

public class LevelPredicate extends AbstractPlayerSelectorPredicate
{
    public static final String KEY_LEVEL = "level";
    public static final String KEY_LEVEL_2 = "l";

    @Override
    public boolean test(Player basis, Player entity, Map<? super String, Object> properties)
    {
        RangedNumber level = (RangedNumber) properties.get(KEY_LEVEL);
        return level.test(entity.getLevel());
    }

    @Override
    public void normalizeMap(Map<? super String, Object> properties)
    {
        integrateAlias(properties, KEY_LEVEL, KEY_LEVEL_2);

        RangedNumber.normalizeMap(KEY_LEVEL, properties);

    }

    @Override
    public String[] getUsingRangedKeys()
    {
        return new String[]{
                KEY_LEVEL,
                KEY_LEVEL_2
        };
    }

    @Override
    public String[] getUsingKeys()
    {
        return new String[]{
                KEY_LEVEL,
                KEY_LEVEL_2
        };
    }
}
