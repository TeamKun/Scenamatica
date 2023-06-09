package net.kunmc.lab.scenamatica.context;

import lombok.Value;
import net.kunmc.lab.scenamatica.interfaces.context.Actor;
import net.kunmc.lab.scenamatica.interfaces.context.Context;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.ContextBean;
import org.bukkit.World;

import java.util.List;

@Value
public class ContextImpl implements Context
{
    World stage;
    List<Actor> actors;
    ContextBean bean;
}
