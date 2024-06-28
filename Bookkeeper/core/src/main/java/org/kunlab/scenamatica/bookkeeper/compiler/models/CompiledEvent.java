package org.kunlab.scenamatica.bookkeeper.compiler.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.LinkedHashMap;
import java.util.Map;

@Value
public class CompiledEvent implements ICompiled
{
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_JAVADOC = "javadoc";
    private static final String KEY_JAVADOC_LINK = "javadoc_link";
    private static final String KEY_SOURCE = "source";

    String id;
    String name;
    String javadoc;
    String javadocLink;
    Source source;
    String description;

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(KEY_ID, this.id);
        map.put(KEY_NAME, this.name);
        map.put(KEY_DESCRIPTION, this.description);
        map.put(KEY_JAVADOC, this.javadoc);
        map.put(KEY_JAVADOC_LINK, this.javadocLink);
        if (this.source != null)
            map.put(KEY_SOURCE, this.source.getId());

        return map;
    }

    @AllArgsConstructor
    @Getter
    public enum Source
    {
        SPIGOT("spigot"),
        PAPER("paper"),
        BUKKIT("bukkit"),
        WATERFALL("waterfall"),
        VELOCITY("velocity"),
        PURPUR("purpur");

        private final String id;

        public static Source fromId(String id)
        {
            for (Source source : values())
                if (source.id.equalsIgnoreCase(id))
                    return source;
            return null;
        }
    }
}
