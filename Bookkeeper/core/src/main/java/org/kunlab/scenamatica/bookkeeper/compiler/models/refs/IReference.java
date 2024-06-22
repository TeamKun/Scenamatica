package org.kunlab.scenamatica.bookkeeper.compiler.models.refs;

import org.kunlab.scenamatica.bookkeeper.compiler.models.ICompiled;

public interface IReference<T extends ICompiled>
{
    String getReference();

    T getResolved();
}
