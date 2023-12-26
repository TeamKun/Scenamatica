package org.kunlab.scenamatica.action.utils;

import com.google.common.reflect.TypeToken;
import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "UnstableApiUsage"})
public class InputTypeToken<T> extends TypeToken<T>
{
    public static <B, U extends B> Class<U> ofBased(Class<B> ignored)
    {
        return new InputTypeToken<U>().toType();
    }

    public static <U> Class<List<U>> ofList(Class<U> ignored)
    {
        return new InputTypeToken<List<U>>().toType();
    }

    public static <K, V> Class<Map<K, V>> ofMap(Class<K> ignoredKey, Class<V> ignoredValue)
    {
        return new InputTypeToken<Map<K, V>>().toType();
    }

    public static <U extends Entity> Class<EntitySpecifier<U>> ofEntity(Class<U> ignored)
    {
        return new InputTypeToken<EntitySpecifier<U>>().toType();
    }

    public Class<T> toType()
    {
        return (Class<T>) this.getRawType();
    }
}
