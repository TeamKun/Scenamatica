package org.kunlab.scenamatica.context;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Value;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.context.ContextManager;
import org.kunlab.scenamatica.interfaces.context.Stage;

import java.util.List;

@Value
public class ContextImpl implements Context
{
    @Getter(AccessLevel.NONE)
    ContextManager manager;

    Stage stage;
    @NotNull
    List<? extends Actor> actors;
    @NotNull
    List<? extends Entity> entities;

    @Override
    public boolean hasStage()
    {
        return this.stage != null;
    }

    @Override
    public boolean hasActors()
    {
        return !this.actors.isEmpty();
    }

    @Override
    public boolean hasEntities()
    {
        return !this.entities.isEmpty();
    }

    @Override
    public void destroy()
    {
        this.manager.destroyContext(this);
    }
}
