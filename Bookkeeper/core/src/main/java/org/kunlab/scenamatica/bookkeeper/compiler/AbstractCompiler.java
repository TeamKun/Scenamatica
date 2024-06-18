package org.kunlab.scenamatica.bookkeeper.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.kunlab.scenamatica.bookkeeper.compiler.models.ICompiled;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.IReference;
import org.kunlab.scenamatica.bookkeeper.definitions.IDefinition;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCompiler<T extends IDefinition, U extends ICompiled, V extends IReference<U>>
        implements ICompiler<T, U, V>
{
    private static final ObjectMapper MAPPER = new ObjectMapper();
    protected final Map<String, V> compiledItemReferences;
    private final String name;

    public AbstractCompiler(String name)
    {
        this.name = name;
        this.compiledItemReferences = new HashMap<>();
    }

    @Override
    public final V compile(T definition)
    {
        String id = toId(definition);
        if (this.compiledItemReferences.containsKey(id))
            return this.compiledItemReferences.get(id);

        V compiled = this.doCompile(definition);
        this.compiledItemReferences.put(id, compiled);

        return compiled;
    }

    @Override
    public void flush(Path directory)
    {
        if (this.name.equals("primitive"))
            return;  // 特別：プリミティブ型はファイル出力しない

        for (V reference : this.compiledItemReferences.values())
        {
            Map<String, Object> serialized = reference.getResolved().serialize();
            Path file = directory.resolve(Paths.get(this.name, this.toId(reference.getResolved()) + ".json"));

            try
            {
                MAPPER.writeValue(file.toFile(), serialized);
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Failed to write compiled item to file", e);
            }
        }
    }

    protected abstract String toId(U compiledItem);

    protected abstract V doCompile(T definition);

    protected abstract String toId(T definition);

    protected String toId(String str)
    {
        StringBuilder id = new StringBuilder();
        for (char c : str.toCharArray())
        {
            if (Character.isAlphabetic(c) || Character.isDigit(c)
                    || c == '_' || c == '-')
                id.append(c);
            else
                id.append('-');
        }

        return id.toString();
    }

    @Override
    public V resolve(String referenceID)
    {
        return this.compiledItemReferences.get(referenceID);
    }

    @Override
    public List<V> getResolvedReferences()
    {
        return List.copyOf(this.compiledItemReferences.values());
    }
}
