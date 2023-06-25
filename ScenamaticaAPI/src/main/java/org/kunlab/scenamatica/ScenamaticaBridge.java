package org.kunlab.scenamatica;

import org.kunlab.scenamatica.interfaces.ScenamaticaRegistry;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Field;

public class ScenamaticaBridge
{
    private static final String PLUGIN_NAME = "Scenamatica";
    private static final String PLUGIN_CLASS = "net.kunmc.lab.scenamatica.Scenamatica";
    private static final String REGISTRY_FIELD = "registry";

    private static ScenamaticaRegistry registry;

    @Nullable
    public static ScenamaticaRegistry retrieveRegistry()
    {
        if (registry == null)
        {
            try
            {
                Class<?> clazz = Class.forName(PLUGIN_CLASS);
                Field field = clazz.getDeclaredField(REGISTRY_FIELD);
                field.setAccessible(true);
                registry = (ScenamaticaRegistry) field.get(Bukkit.getPluginManager().getPlugin(PLUGIN_NAME));
            }
            catch (ClassNotFoundException | IllegalAccessException | NoSuchFieldException e)
            {
                e.printStackTrace();
            }
        }

        return registry;
    }

}
