package org.kunlab.scenamatica.bookkeeper.compiler.models.refs;

import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledAction;

public class ActionReference extends AbstractReference<CompiledAction>
{
    public ActionReference(CompiledAction resolved)
    {
        super(resolved.getName(), resolved);
    }
}
