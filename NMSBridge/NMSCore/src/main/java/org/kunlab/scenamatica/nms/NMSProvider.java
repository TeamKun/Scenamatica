package org.kunlab.scenamatica.nms;

import org.bukkit.Bukkit;

public class NMSProvider
{
    private static String version;
    private static WrapperProvider provider;

    public static WrapperProvider getProvider()
    {
        if (provider != null)
            return provider;

        return retrieveProvider();
    }

    private static WrapperProvider retrieveProvider()
    {
        String version = getServerVersion();
        WrapperProvider provider;
        switch (version)
        {
            case "v1_16_R3":
                provider = new org.kunlab.scenamatica.nms.v1_16_R3.WrapperProviderImpl();
                break;
            default:
                throw new IllegalStateException("Unsupported server version: " + version);
        }

        NMSProvider.version = version;
        return NMSProvider.provider = provider;
    }

    private static String getServerVersion()
    {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }

    public static String getVersion()
    {
        return version;
    }
}
