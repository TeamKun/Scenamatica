package org.kunlab.scenamatica.context;

import lombok.Value;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.Context;

import java.util.List;

@Value
public class ContextImpl implements Context
{
    World stage;
    List<Actor> actors;
    List<Entity> entities;
}
