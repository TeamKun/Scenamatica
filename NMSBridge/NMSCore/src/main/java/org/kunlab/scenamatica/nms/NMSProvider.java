package org.kunlab.scenamatica.nms;

import lombok.Getter;
import org.bukkit.Bukkit;

public class NMSProvider
{
    @Getter
    private static String version;
    private static WrapperProvider provider;
    private static TypeSupport typeSupport;

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
                provider = new org.kunlab.scenamatica.nms.v1_16_R3.WrapperProviderImpl();
                typeSupport = new org.kunlab.scenamatica.nms.v1_16_R3.TypeSupportImpl();
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
