package net.kunmc.lab.scenamatica.scenariofile.beans.context;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.context.PlayerBean;
import net.kunmc.lab.scenamatica.scenariofile.BeanSerializerImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.entity.HumanEntityBeanSerializeTest;
import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerBeanSerializeTest
{
    public static final PlayerBean FULFILLED = new PlayerBeanImpl(
            HumanEntityBeanSerializeTest.FULFILLED,
            "YajuSNPIName",
            false,
            "YajuSNPIDisplay",
            "YajuSenpaiList",
            "YajuListHeader",
            "YajuListFooter",
            new Location(null, 11, 45, 14),
            new Location(null, 11, 45, 14),
            20,
            2000,
            20000,
            true,
            true,
            114f,
            514f,
            4,
            Collections.emptyList()
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(HumanEntityBeanSerializeTest.FULFILLED_MAP)
    {{
        this.put("name", "YajuSNPIName");
        this.put("online", false);
        this.put("display", "YajuSNPIDisplay");
        this.put("playerList", new HashMap<String, Object>()
        {{
            this.put("header", "YajuListHeader");
            this.put("footer", "YajuListFooter");
            this.put("name", "YajuSenpaiList");
        }});
        this.put("compass", new HashMap<String, Object>()
        {{
            this.put("x", 11.0);
            this.put("y", 45.0);
            this.put("z", 14.0);
        }});
        this.put("bedLocation", new HashMap<String, Object>()
        {{
            this.put("x", 11.0);
            this.put("y", 45.0);
            this.put("z", 14.0);
        }});
        this.put("exp", 20);
        this.put("level", 2000);
        this.put("totalExp", 20000);
        this.put("flyable", true);
        this.put("flying", true);
        this.put("walkSpeed", 114f);
        this.put("flySpeed", 514f);
        this.put("op", 4);
    }};

    public static final PlayerBean EMPTY = new PlayerBeanImpl(
            HumanEntityBeanSerializeTest.EMPTY,
            "YajuSNPI",
            true,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            false,
            null,
            null,
            -1,
            Collections.emptyList()
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        this.put("name", "YajuSNPI");
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = PlayerBeanImpl.serialize(FULFILLED, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        PlayerBean bean = PlayerBeanImpl.deserialize(FULFILLED_MAP, BeanSerializerImpl.getInstance());

        assertEquals(FULFILLED, bean);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = PlayerBeanImpl.serialize(EMPTY, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        PlayerBean bean = PlayerBeanImpl.deserialize(EMPTY_MAP, BeanSerializerImpl.getInstance());

        assertEquals(EMPTY, bean);
    }
}
