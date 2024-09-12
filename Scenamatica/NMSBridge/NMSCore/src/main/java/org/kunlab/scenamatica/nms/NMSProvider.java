package org.kunlab.scenamatica.nms;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.kunlab.scenamatica.nms.exceptions.UnsupportedNMSOperationException;
import org.kunlab.scenamatica.nms.types.NMSRegistry;
import org.slf4j.Logger;

public class NMSProvider
{
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(NMSProvider.class);

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
            case "v1_13_R1":
                registry = new org.kunlab.scenamatica.nms.impl.v1_13_R1.NMSRegistryImpl();
                typeSupport = new org.kunlab.scenamatica.nms.impl.v1_13_R1.TypeSupportImpl();
                provider = new org.kunlab.scenamatica.nms.impl.v1_13_R1.WrapperProviderImpl();
                break;
            case "v1_13_R2":
                registry = new org.kunlab.scenamatica.nms.impl.v1_13_R2.NMSRegistryImpl();
                typeSupport = new org.kunlab.scenamatica.nms.impl.v1_13_R2.TypeSupportImpl();
                provider = new org.kunlab.scenamatica.nms.impl.v1_13_R2.WrapperProviderImpl();
                break;
            case "v1_14_R1":
                registry = new org.kunlab.scenamatica.nms.impl.v1_14_R1.NMSRegistryImpl();
                typeSupport = new org.kunlab.scenamatica.nms.impl.v1_14_R1.TypeSupportImpl();
                provider = new org.kunlab.scenamatica.nms.impl.v1_14_R1.WrapperProviderImpl();
                break;
            case "v1_15_R1":
                registry = new org.kunlab.scenamatica.nms.impl.v1_15_R1.NMSRegistryImpl();
                typeSupport = new org.kunlab.scenamatica.nms.impl.v1_15_R1.TypeSupportImpl();
                provider = new org.kunlab.scenamatica.nms.impl.v1_15_R1.WrapperProviderImpl();
                break;
            case "v1_16_R1":
                registry = new org.kunlab.scenamatica.nms.impl.v1_16_R1.NMSRegistryImpl();
                typeSupport = new org.kunlab.scenamatica.nms.impl.v1_16_R1.TypeSupportImpl();
                provider = new org.kunlab.scenamatica.nms.impl.v1_16_R1.WrapperProviderImpl();
                break;
            case "v1_16_R2":
                registry = new org.kunlab.scenamatica.nms.impl.v1_16_R2.NMSRegistryImpl();
                typeSupport = new org.kunlab.scenamatica.nms.impl.v1_16_R2.TypeSupportImpl();
                provider = new org.kunlab.scenamatica.nms.impl.v1_16_R2.WrapperProviderImpl();
                break;
            case "v1_16_R3":
                registry = new org.kunlab.scenamatica.nms.impl.v1_16_R3.NMSRegistryImpl();
                typeSupport = new org.kunlab.scenamatica.nms.impl.v1_16_R3.TypeSupportImpl();
                provider = new org.kunlab.scenamatica.nms.impl.v1_16_R3.WrapperProviderImpl();
                break;
            case "v1_17_R1":
                registry = new org.kunlab.scenamatica.nms.impl.v1_17_R1.NMSRegistryImpl();
                typeSupport = new org.kunlab.scenamatica.nms.impl.v1_17_R1.TypeSupportImpl();
                provider = new org.kunlab.scenamatica.nms.impl.v1_17_R1.WrapperProviderImpl();
                break;
            case "v1_18_R1":
                registry = new org.kunlab.scenamatica.nms.impl.v1_18_R1.NMSRegistryImpl();
                typeSupport = new org.kunlab.scenamatica.nms.impl.v1_18_R1.TypeSupportImpl();
                provider = new org.kunlab.scenamatica.nms.impl.v1_18_R1.WrapperProviderImpl();
                break;
            case "v1_20_R3":
                registry = new org.kunlab.scenamatica.nms.impl.v1_20_R3.NMSRegistryImpl();
                typeSupport = new org.kunlab.scenamatica.nms.impl.v1_20_R3.TypeSupportImpl();
                provider = new org.kunlab.scenamatica.nms.impl.v1_20_R3.WrapperProviderImpl();
                break;
            case "v1_20_R4":
                registry = new org.kunlab.scenamatica.nms.impl.v1_20_R4.NMSRegistryImpl();
                typeSupport = new org.kunlab.scenamatica.nms.impl.v1_20_R4.TypeSupportImpl();
                provider = new org.kunlab.scenamatica.nms.impl.v1_20_R4.WrapperProviderImpl();
                break;
            default:
                throw new IllegalStateException("Unsupported server version: " + version);
        }

        NMSProvider.version = version;
        NMSProvider.provider = provider;
        NMSProvider.typeSupport = typeSupport;
    }

    public static <T> T doNMSSafe(NMSAction<T> action)
    {
        try
        {
            return action.run();
        }
        catch (UnsupportedNMSOperationException e)
        {
            LOGGER.debug("Unsupported NMS operation has been skipped. This is normal behavior and expected in the case.", e);
            return null;
        }
    }

    public static boolean tryDoNMS(NMSVoidAction action)
    {
        try
        {
            action.run();
            return true;
        }
        catch (UnsupportedNMSOperationException e)
        {
            LOGGER.debug("Unsupported NMS operation has been skipped. This is normal behavior and expected in the case.", e);
            return false;
        }
    }

    private static String getServerVersion()
    {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }

    @FunctionalInterface
    public interface NMSAction<T>
    {
        T run() throws UnsupportedNMSOperationException;
    }

    @FunctionalInterface
    public interface NMSVoidAction
    {
        void run() throws UnsupportedNMSOperationException;
    }
}
