package org.kunlab.scenamatica.bookkeeper.compiler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import lombok.Getter;
import org.kunlab.scenamatica.bookkeeper.compiler.models.ICompiled;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.IReference;
import org.kunlab.scenamatica.bookkeeper.definitions.IDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractCompiler<T extends IDefinition, U extends ICompiled, V extends IReference<U>>
        implements ICompiler<T, U, V>
{
    private static final ObjectWriter MAPPER = new ObjectMapper()
            /* 整形する */
            .writerWithDefaultPrettyPrinter();
    protected final Map<String, V> compiledItemReferences;
    @Getter
    private final String name;
    private final Logger log;

    public AbstractCompiler(String name)
    {
        this.name = name;
        this.compiledItemReferences = new HashMap<>();

        this.log = LoggerFactory.getLogger("Compiler/" + name);
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

        this.log.info("Finishing {} compiler...", this.name);

        Path baseDir = directory.resolve(this.name);
        for (V reference : this.compiledItemReferences.values())
        {
            Map<String, Object> serialized = reference.getResolved().serialize();
            Path file = baseDir.resolve(this.getFileLocation(reference));

            try
            {
                Path fileDir = file.getParent();
                if (fileDir != null)
                    fileDir.toFile().mkdirs();

                this.log.debug("Writing compiled item to file: {}", file);
                MAPPER.writeValue(file.toFile(), serialized);
            }
            catch (Exception e)
            {
                throw new IllegalStateException("Failed to write compiled item to file", e);
            }
        }
    }

    protected String getFileLocation(V reference)
    {
        return this.toId(reference.getResolved()) + ".json";
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
        return Collections.unmodifiableList(new ArrayList<>(this.compiledItemReferences.values()));
    }
}
