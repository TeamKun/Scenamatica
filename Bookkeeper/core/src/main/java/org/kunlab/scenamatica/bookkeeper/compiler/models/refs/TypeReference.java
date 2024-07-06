package org.kunlab.scenamatica.bookkeeper.compiler.models.refs;

import org.kunlab.scenamatica.bookkeeper.compiler.CategoryManager;
import org.kunlab.scenamatica.bookkeeper.compiler.TypeCompiler;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledType;

public class TypeReference extends AbstractReference<CompiledType>
{
    public TypeReference(String id, CompiledType resolved)
    {
        super("type", id, resolved);
    }

    @Override
    public String getReference()
    {
        // $ref:type:ID となるが, プリミティブの場合は ID
        return this.resolved instanceof TypeCompiler.PrimitiveType ? this.id: this.getCategorisedReference();
    }

    private String getCategorisedReference()
    {
        CategoryManager.CategoryEntry category = this.resolved.getCategory();
        if (category == null)
            return super.getReference();

        return "$ref:" + this.referenceType + ":" + category.getId() + ":" + this.id;
    }
}
