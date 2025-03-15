package org.kunlab.scenamatica.action.actions.base.world.weather;

import org.bukkit.event.Event;
import org.bukkit.event.weather.WeatherEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.base.world.AbstractWorldAction;
import org.kunlab.scenamatica.interfaces.action.ActionContext;

public abstract class AbstractWeatherAction extends AbstractWorldAction
{
    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof WeatherEvent))
            return false;

        WeatherEvent e = (WeatherEvent) event;
        return this.checkMatchedWorld(ctxt, e.getWorld());
    }
}
