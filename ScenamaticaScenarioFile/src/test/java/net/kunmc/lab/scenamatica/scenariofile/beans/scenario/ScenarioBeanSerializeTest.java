package net.kunmc.lab.scenamatica.scenariofile.beans.scenario;

import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScenarioBeanSerializeTest
{
    public static final ScenarioBean FULFILLED = new ScenarioBean(
            ScenarioType.ACTION_EXECUTE,
            ActionBeanSerializeTest.FULFILLED,
            114514L
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(ActionBeanSerializeTest.FULFILLED_MAP)
    {{
        this.put("type", "execute");
        this.put("timeout", 114514L);
    }};

    public static final ScenarioBean EMPTY = new ScenarioBean(
            ScenarioType.ACTION_EXECUTE,
            ActionBeanSerializeTest.EMPTY,
            -1L
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>(ActionBeanSerializeTest.EMPTY_MAP)
    {{
        this.put("type", "execute");
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = ScenarioBean.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ScenarioBean bean = ScenarioBean.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, bean);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = ScenarioBean.serialize(EMPTY);

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ScenarioBean bean = ScenarioBean.deserialize(EMPTY_MAP);

        assertEquals(EMPTY, bean);
    }
}
