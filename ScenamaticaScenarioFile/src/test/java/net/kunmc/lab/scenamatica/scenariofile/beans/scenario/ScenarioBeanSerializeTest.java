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
}
