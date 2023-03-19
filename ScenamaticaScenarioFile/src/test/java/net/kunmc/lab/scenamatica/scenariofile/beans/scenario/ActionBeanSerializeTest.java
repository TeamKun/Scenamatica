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

    public static final ActionBean EMPTY = new ActionBean(
            "action",
            null
    );

    public static final HashMap<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        this.put("action", "action");
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

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = ActionBean.serialize(EMPTY);

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ActionBean bean = ActionBean.deserialize(EMPTY_MAP);

        assertEquals(EMPTY, bean);
    }
}
