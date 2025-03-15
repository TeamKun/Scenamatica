package org.kunlab.scenamatica.selector.predicates;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.selector.compiler.AmbiguousString;

import java.util.Map;

public class NamePredicate extends AbstractGeneralEntitySelectorPredicate
{
    public static final String KEY_NAME = "name";

    @Override
    public boolean test(Player basis, Entity entity, Map<? super String, Object> properties)
    {
        String name = entity.getName();
        AmbiguousString ambiguousString = (AmbiguousString) properties.get(KEY_NAME);
        if (ambiguousString == null)
            return true;

        return ambiguousString.test(name);
    }

    @Override
    public void normalizeMap(Map<? super String, Object> properties)
    {
        AmbiguousString.normalizeMap(KEY_NAME, properties);
    }

    @Override
    public String[] getUsingAmbiguousStringKeys()
    {
        return new String[]{KEY_NAME};
    }

    @Override
    public String[] getUsingKeys()
    {
        return new String[]{KEY_NAME};
    }
}
