package net.kunmc.lab.scenamatica.scenariofile.beans.misc;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockBeanSerializeTest
{
    public static final BlockBean FULFILLED = new BlockBeanImpl(
            Material.ACACIA_SIGN,
            114,
            514,
            19,
            new HashMap<String, Object>()
            {{
                put("key", "value");
            }},
            15,
            Biome.BADLANDS
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        put("type", "ACACIA_SIGN");
        put("x", 114);
        put("y", 514);
        put("z", 19);
        put("metadata", new HashMap<String, Object>()
        {{
            put("key", "value");
        }});
        put("light", 15);
        put("biome", "BADLANDS");
    }};

    public static final BlockBean EMPTY = new BlockBeanImpl(
            Material.AIR,
            0,
            0,
            0,
            Collections.emptyMap(),
            0,
            null
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        put("type", "AIR");
        put("x", 0);
        put("y", 0);
        put("z", 0);
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = BlockBeanImpl.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        BlockBean bean = BlockBeanImpl.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, bean);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = BlockBeanImpl.serialize(EMPTY);
        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        BlockBean bean = BlockBeanImpl.deserialize(EMPTY_MAP);
        assertEquals(EMPTY, bean);
    }
}
