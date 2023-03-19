package net.kunmc.lab.scenamatica.scenariofile.beans.context;

import net.kunmc.lab.scenamatica.scenariofile.beans.entity.HumanEntityBeanSerializeTest;
import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.bukkit.Location;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerBeanSerializeTest
{
    public static final PlayerBean FULFILLED = new PlayerBean(
            HumanEntityBeanSerializeTest.FULFILLED,
            "YajuSNPIName",
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
            514f
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(HumanEntityBeanSerializeTest.FULFILLED_MAP)
    {{
        this.put("name", "YajuSNPIName");
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
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = PlayerBean.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        PlayerBean bean = PlayerBean.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, bean);
    }
}
