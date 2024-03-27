package org.kunlab.scenamatica.exceptions.context.actor;

public class ActorAlreadyExistsException extends ActorCreationException
{
    public ActorAlreadyExistsException(String actorName)
    {
        super("Actor " + actorName + " already exists.");
    }
}
