package org.kunlab.scenamatica.exceptions.context.actor;

import org.kunlab.scenamatica.exceptions.context.ContextPreparationException;

public class ActorAlreadyExistsException extends ContextPreparationException
{
    public ActorAlreadyExistsException(String actorName)
    {
        super("Actor " + actorName + " already exists.");
    }
}
