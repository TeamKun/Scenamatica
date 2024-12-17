package org.kunlab.scenamatica.nms.enums.entity;

import org.bukkit.event.weather.LightningStrikeEvent;

public enum NMSLightningStrikeCause
{
    COMMAND,
    TRIDENT,
    TRAP,
    WEATHER,
    UNKNOWN;

    public static NMSLightningStrikeCause fromBukkit(LightningStrikeEvent.Cause cause)
    {
        if (cause == null)
            return null;

        switch (cause)
        {
            case COMMAND:
                return COMMAND;
            case TRIDENT:
                return TRIDENT;
            case TRAP:
                return TRAP;
            case WEATHER:
                return WEATHER;
            default:
                return UNKNOWN;
        }
    }

    public static LightningStrikeEvent.Cause toBukkit(NMSLightningStrikeCause cause)
    {
        switch (cause)
        {
            case COMMAND:
                return LightningStrikeEvent.Cause.COMMAND;
            case TRIDENT:
                return LightningStrikeEvent.Cause.TRIDENT;
            case TRAP:
                return LightningStrikeEvent.Cause.TRAP;
            case WEATHER:
                return LightningStrikeEvent.Cause.WEATHER;
            default:
                return null;
        }
    }
}
