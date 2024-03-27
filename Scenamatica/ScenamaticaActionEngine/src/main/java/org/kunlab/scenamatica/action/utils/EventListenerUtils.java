package org.kunlab.scenamatica.action.utils;

import lombok.experimental.UtilityClass;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

@UtilityClass
public class EventListenerUtils
{
    @NotNull
    public static HandlerList getListeners(@NotNull Class<? extends Event> type)
    {
        try
        {
            Method method = getEventClass(type).getDeclaredMethod("getHandlerList");
            method.setAccessible(true);
            return (HandlerList) method.invoke(null);
        }
        catch (Exception e)
        {
            throw new IllegalStateException("The event " + type.getName() + " is not event. " +
                    " Please contact the developer of the plugin " + type.getName() + " to fix this issue.", e);
        }
    }

    @NotNull
    public static Class<? extends Event> getEventClass(@NotNull Class<? extends Event> clazz)
    {
        try
        {
            clazz.getDeclaredMethod("getHandlerList");
            return clazz;
        }
        catch (NoSuchMethodException e)
        {
            if (clazz.getSuperclass() != null
                    && !clazz.getSuperclass().equals(Event.class)
                    && Event.class.isAssignableFrom(clazz.getSuperclass()))
            {
                return getEventClass(clazz.getSuperclass().asSubclass(Event.class));
            }
            else
                throw new IllegalStateException("Unable to find handler list for event " + clazz.getName() + ". " +
                        "Please contact the developer of the plugin " + clazz.getName() + " to fix this issue.", e);
        }
    }
}
