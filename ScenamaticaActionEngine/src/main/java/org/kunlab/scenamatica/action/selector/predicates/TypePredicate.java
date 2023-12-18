package org.kunlab.scenamatica.action.selector.predicates;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.kunlab.scenamatica.commons.utils.Utils;

import java.util.Map;

public class TypePredicate extends AbstractGeneralEntitySelectorPredicate
{
    public static final String KEY_TYPE = "type";

    @Override
    public boolean test(Player basis, Entity entity, Map<? super String, Object> properties)
    {
        EntityType type = (EntityType) properties.get(KEY_TYPE);
        return entity.getType() == type;
    }

    @Override
    public void normalizeMap(Map<? super String, Object> properties)
    {
        String type = normalizeString(properties.get(KEY_TYPE));
        if (type == null)
            throw new IllegalArgumentException("entity type cannot be null");

        properties.put(KEY_TYPE, Utils.searchEntityType(type));
    }

    @Override
    public String[] getUsingKeys()
    {
        return new String[]{KEY_TYPE};
    }
}
