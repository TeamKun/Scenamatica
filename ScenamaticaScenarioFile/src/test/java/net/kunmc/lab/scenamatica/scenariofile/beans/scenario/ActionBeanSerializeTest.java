package net.kunmc.lab.scenamatica.scenariofile.beans.scenario;

import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActionBeanSerializeTest
{
    public static final ActionBean FULFILLED = new ActionBean(
            "action",
            new HashMap<String, Object>()
            {{
                this.put("action", "action123");
            }}
    );

    public static final HashMap<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("action", "action");
        this.put("with", new HashMap<String, Object>()
        {{
            this.put("action", "action123");
        }});
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = ActionBean.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ActionBean bean = ActionBean.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, bean);
    }
}
