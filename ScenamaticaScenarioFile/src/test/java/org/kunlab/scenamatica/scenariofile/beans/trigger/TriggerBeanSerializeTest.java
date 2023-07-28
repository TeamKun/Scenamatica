package org.kunlab.scenamatica.scenariofile.beans.trigger;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerBean;
import org.kunlab.scenamatica.scenariofile.BeanSerializerImpl;
import org.kunlab.scenamatica.scenariofile.beans.scenario.ScenarioBeanSerializeTest;
import org.kunlab.scenamatica.scenariofile.beans.utils.MapTestUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TriggerBeanSerializeTest
{
    public static final TriggerBean FULFILLED = new TriggerBeanImpl(
            TriggerType.ON_ACTION,
            null,
            Arrays.asList(ScenarioBeanSerializeTest.FULFILLED, ScenarioBeanSerializeTest.FULFILLED),
            Arrays.asList(ScenarioBeanSerializeTest.FULFILLED, ScenarioBeanSerializeTest.FULFILLED),
            null
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("type", "on_action");
        this.put("before", Arrays.asList(ScenarioBeanSerializeTest.FULFILLED_MAP, ScenarioBeanSerializeTest.FULFILLED_MAP));
        this.put("after", Arrays.asList(ScenarioBeanSerializeTest.FULFILLED_MAP, ScenarioBeanSerializeTest.FULFILLED_MAP));
    }};

    public static final TriggerBean EMPTY = new TriggerBeanImpl(
            TriggerType.ON_ACTION,
            null,
            Collections.emptyList(),
            Collections.emptyList(),
            null
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        this.put("type", "on_action");
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = TriggerBeanImpl.serialize(FULFILLED, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        TriggerBean bean = TriggerBeanImpl.deserialize(FULFILLED_MAP, BeanSerializerImpl.getInstance());

        assertEquals(FULFILLED, bean);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = TriggerBeanImpl.serialize(EMPTY, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        TriggerBean bean = TriggerBeanImpl.deserialize(EMPTY_MAP, BeanSerializerImpl.getInstance());

        assertEquals(EMPTY, bean);
    }
}
