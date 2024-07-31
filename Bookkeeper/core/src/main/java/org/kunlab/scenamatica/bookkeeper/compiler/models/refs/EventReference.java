package org.kunlab.scenamatica.bookkeeper.compiler.models.refs;

import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledEvent;

public class EventReference extends AbstractReference<CompiledEvent>
{
    public EventReference(String id, CompiledEvent event)
    {
        super("event", id, event);
    }
}
