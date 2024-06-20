package org.kunlab.scenamatica.bookkeeper.compiler.models.refs;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.kunlab.scenamatica.bookkeeper.compiler.models.ICompiled;

@ToString
@EqualsAndHashCode
public abstract class AbstractReference<T extends ICompiled> implements IReference<T>
{
    protected final String referenceType;
    protected final String id;
    protected final T resolved;

    public AbstractReference(String referenceType, String id, T resolved)
    {
        this.referenceType = referenceType;
        this.id = id;
        this.resolved = resolved;
    }

    @Override
    public T getResolved()
    {
        return this.resolved;
    }

    @Override
    public String getID()
    {
        return "$ref:" + this.referenceType + ":" + this.id;
    }

}
