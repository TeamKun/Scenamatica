package org.kunlab.scenamatica.scenariofile.structures.context;

import org.bukkit.World;
import org.bukkit.WorldType;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.structures.context.StageStructure;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StageStructureSerializeTest
{

    public static final StageStructure FULFILLED = new StageStructureImpl(
            "world",
            WorldType.AMPLIFIED,
            1145141919810L,
            false,
            World.Environment.NORMAL,
            true
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("copyOf", "world");
        this.put("type", "AMPLIFIED");
        this.put("seed", 1145141919810L);
        this.put("structures", false);
        this.put("env", "NORMAL");
        this.put("hardcore", true);
    }};

    public static final StageStructure EMPTY = new StageStructureImpl(
            "world",
            WorldType.NORMAL,
            null,
            true,
            null,
            false
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        this.put("copyOf", "world");
    }};

    @Test
    void 正常シリアライズできるか()
    {
        Map<String, Object> actual = StageStructureImpl.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, actual);
    }

    @Test
    void 正常デシリアライズできるか()
    {
        StageStructure actual = StageStructureImpl.deserialize(FULFILLED_MAP);

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
                IllegalArgumentException.class,
                () -> StageStructureImpl.deserialize(map)
        );
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> actual = StageStructureImpl.serialize(EMPTY);

        MapTestUtil.assertEqual(EMPTY_MAP, actual);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        StageStructure actual = StageStructureImpl.deserialize(EMPTY_MAP);

        assertEquals(EMPTY, actual);
    }
}
