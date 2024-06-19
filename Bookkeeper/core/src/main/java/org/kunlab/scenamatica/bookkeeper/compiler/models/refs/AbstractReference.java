package org.kunlab.scenamatica.bookkeeper.compiler.models.refs;

import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.kunlab.scenamatica.bookkeeper.compiler.models.ICompiled;

@ToString
@EqualsAndHashCode
public abstract class AbstractReference<T extends ICompiled> implements IReference<T>
{
    private final String id;
    private final T resolved;

    public AbstractReference(String id, T resolved)
    {
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
        return this.id;
    }

}
