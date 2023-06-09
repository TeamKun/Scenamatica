package org.kunlab.scenamatica.scenariofile.beans.scenario;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.action.ActionBean;
import org.kunlab.scenamatica.scenariofile.beans.utils.MapTestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActionBeanSerializeTest
{
    public static final ActionBean FULFILLED = new ActionBeanImpl(
            "none",
            null
    );

    public static final HashMap<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("action", "none");
    }};

    public static final ActionBean EMPTY = new ActionBeanImpl(
            "none",
            null
    );

    public static final HashMap<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        this.put("action", "none");
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = ActionBeanImpl.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ActionBean bean = ActionBeanImpl.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, bean);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = ActionBeanImpl.serialize(EMPTY);

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ActionBean bean = ActionBeanImpl.deserialize(EMPTY_MAP);

        assertEquals(EMPTY, bean);
    }
}
