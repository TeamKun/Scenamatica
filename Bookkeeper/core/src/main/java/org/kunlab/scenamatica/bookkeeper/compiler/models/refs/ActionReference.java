package org.kunlab.scenamatica.bookkeeper.compiler.models.refs;

import org.kunlab.scenamatica.bookkeeper.compiler.CategoryManager;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledAction;

public class ActionReference extends AbstractReference<CompiledAction>
{
    public ActionReference(CompiledAction resolved)
    {
        super("action", resolved.getId(), resolved);
    }

    @Override
    public String getReference()
    {
        return this.getCategorisedReference();
    }

    private String getCategorisedReference()
    {
        CategoryManager.CategoryEntry category = this.resolved.getCategory();
        if (category == null)
            return super.getReference();

        return "$ref:" + this.referenceType + ":" + category.getId() + ":" + this.id;
    }
}
