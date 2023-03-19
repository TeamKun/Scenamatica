package net.kunmc.lab.scenamatica.scenariofile.beans.entity;

import net.kunmc.lab.scenamatica.scenariofile.beans.entities.EntityBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityBeanSerializeTest
{
    public static final EntityBean FULFILLED = new EntityBean(
            new Location(null, 1145, 1419, 19, 8, 10),
            "YajuSNPI",
            UUID.fromString("a1b1c4d5-e1f4-a1b9-c1d9-e8f1a0bcdef1"),
            true,
            false,
            Arrays.asList("tagTest", "tagTest2"),
            22,
            20,
            DamageBeanSerializeTest.FULFILLED
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("loc", new HashMap<String, Object>()
        {{
            this.put("x", 1145.0);
            this.put("y", 1419.0);
            this.put("z", 19.0);
            this.put("yaw", 8.0f);
            this.put("pitch", 10.0f);
        }});
        this.put("customName", "YajuSNPI");

        this.put("uuid", "a1b1c4d5-e1f4-a1b9-c1d9-e8f1a0bcdef1");
        this.put("glowing", true);
        this.put("gravity", false);
        this.put("tags", Arrays.asList("tagTest", "tagTest2"));
        this.put("maxHealth", 22);
        this.put("health", 20);
        this.put("lastDamage", DamageBeanSerializeTest.FULFILLED_MAP);
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = EntityBean.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        EntityBean entity = EntityBean.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, entity);
    }
}
