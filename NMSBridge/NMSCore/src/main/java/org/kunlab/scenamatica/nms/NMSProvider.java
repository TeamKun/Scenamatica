package org.kunlab.scenamatica.nms;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.kunlab.scenamatica.nms.types.NMSRegistry;

public class NMSProvider
{
    @Getter
    private static String version;
    private static WrapperProvider provider;
    private static TypeSupport typeSupport;
    private static NMSRegistry registry;

    public static WrapperProvider getProvider()
    {
        if (provider != null)
            return provider;

        initAll();
        return provider;
    }

    public static TypeSupport getTypeSupport()
    {
        if (typeSupport != null)
            return typeSupport;

        initAll();
        return typeSupport;
    }

    private static void initAll()
    {
        WrapperProvider provider;
        TypeSupport typeSupport;
        String version = getServerVersion();
        switch (version)
        {
            case "v1_16_R3":
                registry = new org.kunlab.scenamatica.nms.impl.v1_16_R3.NMSRegistryImpl();
                typeSupport = new org.kunlab.scenamatica.nms.impl.v1_16_R3.TypeSupportImpl();
                provider = new org.kunlab.scenamatica.nms.impl.v1_16_R3.WrapperProviderImpl();
                break;
            default:
                throw new IllegalStateException("Unsupported server version: " + version);
        }

        NMSProvider.version = version;
        NMSProvider.provider = provider;
        NMSProvider.typeSupport = typeSupport;
    }

    private static String getServerVersion()
    {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }
}
