package org.kunlab.scenamatica.bookkeeper.compiler.models.refs;

import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledCategory;

public class CategoryReference extends AbstractReference<CompiledCategory>
{
    public CategoryReference(CompiledCategory resolved)
    {
        super("category", resolved.getId(), resolved);
    }
}
