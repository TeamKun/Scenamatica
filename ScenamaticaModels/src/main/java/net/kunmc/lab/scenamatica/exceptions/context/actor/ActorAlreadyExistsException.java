package net.kunmc.lab.scenamatica.exceptions.context.actor;

import net.kunmc.lab.scenamatica.exceptions.context.*;

public class ActorAlreadyExistsException extends ContextPreparationException
{
    public ActorAlreadyExistsException(String actorName)
    {
        super("Actor " + actorName + " already exists.");
    }
}
