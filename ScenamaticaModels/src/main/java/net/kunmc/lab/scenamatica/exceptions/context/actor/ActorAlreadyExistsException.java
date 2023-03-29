package net.kunmc.lab.scenamatica.exceptions.context.actor;

import net.kunmc.lab.scenamatica.exceptions.context.ContextPreparationException;

public class ActorAlreadyExistsException extends ContextPreparationException
{
    private final String actorName;

    public ActorAlreadyExistsException(String actorName)
    {
        super("Actor " + actorName + " already exists.");
        this.actorName = actorName;
    }
}
