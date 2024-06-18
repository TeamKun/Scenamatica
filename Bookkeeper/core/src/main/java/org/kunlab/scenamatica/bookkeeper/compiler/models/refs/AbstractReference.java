package org.kunlab.scenamatica.bookkeeper.compiler.models.refs;

import org.kunlab.scenamatica.bookkeeper.compiler.models.ICompiled;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof AbstractReference<?> that)) return false;
        return Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(this.id);
    }
}
