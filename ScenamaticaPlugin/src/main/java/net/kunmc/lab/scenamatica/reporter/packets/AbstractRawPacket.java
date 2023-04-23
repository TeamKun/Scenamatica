package net.kunmc.lab.scenamatica.reporter.packets;

import lombok.AllArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public abstract class AbstractRawPacket
{
    private static final String KEY_GENRE = "genre";
    private static final String KEY_TYPE = "type";

    private static final String KEY_DATE = "date";

    @NotNull
    private final String genre;
    @NotNull
    private final String type;

    public Map<String, Object> serialize()
    {
        Map<String, Object> result = new HashMap<>();

        result.put(KEY_GENRE, this.genre);
        result.put(KEY_TYPE, this.type);
        result.put(KEY_DATE, System.currentTimeMillis());

        return result;
    }
}
