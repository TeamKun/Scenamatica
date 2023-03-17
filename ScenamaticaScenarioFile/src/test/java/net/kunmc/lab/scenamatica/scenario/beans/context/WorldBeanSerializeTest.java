package net.kunmc.lab.scenamatica.scenario.beans.context;

import net.kunmc.lab.scenamatica.scenario.beans.utils.MapTestUtil;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class WorldBeanSerializeTest
{

    private final WorldBean fulfilledBean = new WorldBean("test",
            WorldType.AMPLIFIED,
            1145141919810L,
            true,
            World.Environment.NORMAL,
            true
    );

    private final Map<String, Object> fulfilledMap = new HashMap<String, Object>() {{
        put("name", "test");
        put("type", "AMPLIFIED");
        put("seed", 1145141919810L);
        put("structures", true);
        put("env", "NORMAL");
        put("hardcore", true);
    }};

    @Test
    void 正常シリアライズできるか()
    {
        Map<String, Object> actual = WorldBean.serialize(this.fulfilledBean);

        MapTestUtil.assertEqual(this.fulfilledMap, actual);
    }

    @Test
    void 正常デシリアライズできるか()
    {
        WorldBean actual = WorldBean.deserialize(this.fulfilledMap);

        assertEquals(this.fulfilledBean, actual);
    }

    @Test
    void 必須項目なしがデシリアライズできないか()
    {
        Map<String, Object> map = new HashMap<>(this.fulfilledMap);
        map.remove("name");

        assertThrows(
                IllegalArgumentException.class,
                () -> WorldBean.deserialize(map)
        );
    }

    @Test
    void 不正な型があるとデシリアライズできないか()
    {
        Map<String, Object> map = new HashMap<>(this.fulfilledMap);
        map.put("name", 114514L);
        map.put("type", 1919810L);
        map.put("seed", "1145141919810");
        map.put("structures", "foobar");
        map.put("env", 1145141919810L);

        assertThrows(
                IllegalArgumentException.class,
                () -> WorldBean.deserialize(map)
        );
    }
}
