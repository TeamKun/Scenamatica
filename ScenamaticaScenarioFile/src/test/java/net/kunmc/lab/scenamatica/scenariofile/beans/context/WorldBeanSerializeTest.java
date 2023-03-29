package net.kunmc.lab.scenamatica.scenariofile.beans.context;

import net.kunmc.lab.scenamatica.exceptions.scenariofile.InvalidScenarioFileException;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.WorldBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WorldBeanSerializeTest
{

    public static final WorldBean FULFILLED = new WorldBeanImpl(
            "world",
            WorldType.AMPLIFIED,
            1145141919810L,
            false,
            World.Environment.NORMAL,
            true
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        put("copyOf", "world");
        put("type", "AMPLIFIED");
        put("seed", 1145141919810L);
        put("structures", false);
        put("env", "NORMAL");
        put("hardcore", true);
    }};

    public static final WorldBean EMPTY = new WorldBeanImpl(
            "world",
            WorldType.NORMAL,
            null,
            true,
            null,
            false
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        put("copyOf", "world");
    }};

    @Test
    void 正常シリアライズできるか()
    {
        Map<String, Object> actual = WorldBeanImpl.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, actual);
    }

    @Test
    void 正常デシリアライズできるか()
    {
        WorldBean actual = WorldBeanImpl.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, actual);
    }

    @Test
    void 不正な型があるとデシリアライズできないか()
    {
        Map<String, Object> map = new HashMap<>(FULFILLED_MAP);
        map.put("copyOf", 114514L);
        map.put("type", 1919810L);
        map.put("seed", "1145141919810");
        map.put("structures", "foobar");
        map.put("env", 1145141919810L);

        assertThrows(
                InvalidScenarioFileException.class,
                () -> WorldBeanImpl.deserialize(map)
        );
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> actual = WorldBeanImpl.serialize(EMPTY);

        MapTestUtil.assertEqual(EMPTY_MAP, actual);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        WorldBean actual = WorldBeanImpl.deserialize(EMPTY_MAP);

        assertEquals(EMPTY, actual);
    }
}
