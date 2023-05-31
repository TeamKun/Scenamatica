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
                this.put("key", "value");
            }},
            15,
            Biome.BADLANDS
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("type", "ACACIA_SIGN");
        this.put("x", 114);
        this.put("y", 514);
        this.put("z", 19);
        this.put("metadata", new HashMap<String, Object>()
        {{
            this.put("key", "value");
        }});
        this.put("light", 15);
        this.put("biome", "BADLANDS");
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
        this.put("type", "AIR");
        this.put("x", 0);
        this.put("y", 0);
        this.put("z", 0);
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
