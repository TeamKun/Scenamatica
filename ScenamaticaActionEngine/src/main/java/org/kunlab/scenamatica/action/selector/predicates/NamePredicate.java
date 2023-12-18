package org.kunlab.scenamatica.action.selector.predicates;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.action.selector.compiler.AmbiguousString;

import java.util.Map;

public class NamePredicate extends AbstractGeneralEntitySelectorPredicate
{
    public static final String KEY_NAME = "name";

    @Override
    public boolean test(Player basis, Entity entity, Map<? super String, Object> properties)
    {
        String nameDef = (String) properties.get(KEY_NAME);
        if (nameDef == null)
            return true;

        String name = entity.getName();
        AmbiguousString ambiguousString = AmbiguousString.fromMap(KEY_NAME, properties);

        return ambiguousString.test(name);
    }

    @Override
    public void normalizeMap(Map<? super String, Object> properties)
    {
        AmbiguousString.normalizeMap(KEY_NAME, KEY_NAME, properties);
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
