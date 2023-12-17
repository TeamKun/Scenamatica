package org.kunlab.scenamatica.commons.utils;

import com.destroystokyo.paper.Namespaced;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

@UtilityClass
public class NamespaceUtils
{
    @SuppressWarnings("deprecation")
    public static NamespacedKey fromString(String str)
    {
        String[] split = str.split(":", 2);
        if (split.length == 1)
            return new NamespacedKey("minecraft", split[0]);
        else
            return new NamespacedKey(split[0], split[1]);
    }

    public static String toString(Namespaced namespaced)
    {
        String namespace = namespaced.getNamespace();
        String key = namespaced.getKey();

        if (namespace.equals("minecraft"))
            return key;
        else
            return namespace + ":" + key;
    }

    public static boolean equalsIgnoreCase(@Nullable NamespacedKey key1, @Nullable NamespacedKey key2)
    {
        if (key1 == null || key2 == null)
            return key1 == key2;

        return key1.getNamespace().equalsIgnoreCase(key2.getNamespace())
                && key1.getKey().equalsIgnoreCase(key2.getKey());
    }
}
