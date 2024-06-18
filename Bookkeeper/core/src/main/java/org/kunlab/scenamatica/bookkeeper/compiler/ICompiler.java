package org.kunlab.scenamatica.bookkeeper.compiler;

import org.kunlab.scenamatica.bookkeeper.compiler.models.ICompiled;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.IReference;
import org.kunlab.scenamatica.bookkeeper.definitions.IDefinition;

import java.nio.file.Path;
import java.util.List;

public interface ICompiler<T extends IDefinition, U extends ICompiled, V extends IReference<U>>
{
    default void init()
    {

    }

    Class<T> getDefinitionType();

    void flush(Path directory);

    V compile(T definition);

    V resolve(String referenceID);

    default V postResolve(V reference)
    {
        return reference;
    }

    List<V> getResolvedReferences();
}
