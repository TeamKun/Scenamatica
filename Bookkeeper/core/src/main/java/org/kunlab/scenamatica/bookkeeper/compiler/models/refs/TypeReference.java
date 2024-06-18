package org.kunlab.scenamatica.bookkeeper.compiler.models.refs;

import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledType;

public class TypeReference extends AbstractReference<CompiledType>
{
    public TypeReference(String id, CompiledType resolved)
    {
        super(id, resolved);
    }
}
