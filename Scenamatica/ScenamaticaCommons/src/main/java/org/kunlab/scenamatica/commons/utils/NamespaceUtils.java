package org.kunlab.scenamatica.commons.utils;

import com.destroystokyo.paper.Namespaced;
import lombok.experimental.UtilityClass;
import org.bukkit.NamespacedKey;

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
}
