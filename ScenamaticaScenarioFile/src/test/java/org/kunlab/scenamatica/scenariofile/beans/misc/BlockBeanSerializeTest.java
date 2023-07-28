package org.kunlab.scenamatica.scenariofile.beans.misc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockBean;
import org.kunlab.scenamatica.scenariofile.beans.utils.MapTestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockBeanSerializeTest
{
    public static final BlockBean FULFILLED = new BlockBeanImpl(
            Material.ACACIA_SIGN,
            new Location(null, 114, 514, 19),
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
        this.put("location", new HashMap<String, Object>()
        {{
            this.put("x", 114.0);
            this.put("y", 514.0);
            this.put("z", 19.0);
        }});
        this.put("metadata", new HashMap<String, Object>()
        {{
            this.put("key", "value");
        }});
        this.put("light", 15);
        this.put("biome", "BADLANDS");
    }};

    public static final BlockBean EMPTY = new BlockBeanImpl(
            null,
            null,
            Collections.emptyMap(),
            null,
            null
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<>();

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

        assertEquals(bean, FULFILLED);
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
