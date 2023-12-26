package org.kunlab.scenamatica.action.utils;

import com.google.common.reflect.TypeToken;
import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.List;
import java.util.Map;

@SuppressWarnings({"unchecked", "UnstableApiUsage"})
public class InputTypeToken<T>
{
    public static <B, U extends B> Class<U> ofBased(Class<B> ignored)
    {
        return (Class<U>) new TypeToken<U>(ignored)
        {
        }.getRawType();
    }

    public static <U> Class<List<U>> ofList(Class<U> ignored)
    {
        return (Class<List<U>>) new TypeToken<List<U>>(List.class)
        {
        }.getRawType();
    }

    public static <K, V> Class<Map<K, V>> ofMap(Class<K> ignoredKey, Class<V> ignoredValue)
    {
        return (Class<Map<K, V>>) new TypeToken<Map<K, V>>(Map.class)
        {
        }.getRawType();
    }

    public static <U extends Entity> Class<EntitySpecifier<U>> ofEntity(Class<U> ignored)
    {
        return (Class<EntitySpecifier<U>>) new TypeToken<EntitySpecifier<U>>(EntitySpecifier.class)
        {
        }.getRawType();
    }
}
