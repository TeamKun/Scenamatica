package org.kunlab.scenamatica.bookkeeper.compiler.models;

import java.util.HashMap;
import java.util.Map;

public record CompiledCategory(String id, String name, String description) implements ICompiled
{
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_DESCRIPTION = "description";

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_ID, this.id);
        map.put(KEY_NAME, this.name);
        map.put(KEY_DESCRIPTION, this.description);

        return map;
    }
}
