package org.kunlab.scenamatica.scenariofile.beans.scenario;

import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.scenariofile.scenario.ScenarioBean;
import org.kunlab.scenamatica.scenariofile.BeanSerializerImpl;
import org.kunlab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScenarioBeanSerializeTest
{
    public static final ScenarioBean FULFILLED = new ScenarioBeanImpl(
            ScenarioType.ACTION_EXECUTE,
            ActionBeanSerializeTest.FULFILLED,
            null,
            114514L
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(ActionBeanSerializeTest.FULFILLED_MAP)
    {{
        this.put("type", "execute");
        this.put("timeout", 114514L);
    }};

    public static final ScenarioBean EMPTY = new ScenarioBeanImpl(
            ScenarioType.ACTION_EXECUTE,
            ActionBeanSerializeTest.EMPTY,
            null,
            -1L
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>(ActionBeanSerializeTest.EMPTY_MAP)
    {{
        this.put("type", "execute");
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = ScenarioBeanImpl.serialize(FULFILLED, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ScenarioBean bean = ScenarioBeanImpl.deserialize(FULFILLED_MAP, BeanSerializerImpl.getInstance());

        assertEquals(FULFILLED, bean);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = ScenarioBeanImpl.serialize(EMPTY, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ScenarioBean bean = ScenarioBeanImpl.deserialize(EMPTY_MAP, BeanSerializerImpl.getInstance());

        assertEquals(EMPTY, bean);
    }
}
