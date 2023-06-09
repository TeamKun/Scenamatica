package org.kunlab.scenamatica.scenariofile.beans.context;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.context.ContextBean;
import org.kunlab.scenamatica.scenariofile.BeanSerializerImpl;
import org.kunlab.scenamatica.scenariofile.beans.utils.MapTestUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextBeanSerializeTest
{
    public static final ContextBeanImpl FULFILLED = new ContextBeanImpl(
            Arrays.asList(
                    PlayerBeanSerializeTest.FULFILLED,
                    PlayerBeanSerializeTest.FULFILLED,
                    PlayerBeanSerializeTest.FULFILLED
            ),
            StageBeanSerializeTest.FULFILLED
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("actors", Arrays.asList(
                PlayerBeanSerializeTest.FULFILLED_MAP,
                PlayerBeanSerializeTest.FULFILLED_MAP,
                PlayerBeanSerializeTest.FULFILLED_MAP
        ));
        this.put("stage", StageBeanSerializeTest.FULFILLED_MAP);
    }};

    public static final ContextBeanImpl EMPTY = new ContextBeanImpl(
            Collections.emptyList(),
            null
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = ContextBeanImpl.serialize(FULFILLED, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ContextBean bean = ContextBeanImpl.deserialize(FULFILLED_MAP, BeanSerializerImpl.getInstance());

        assertEquals(FULFILLED, bean);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = ContextBeanImpl.serialize(EMPTY, BeanSerializerImpl.getInstance());
        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ContextBean bean = ContextBeanImpl.deserialize(EMPTY_MAP, BeanSerializerImpl.getInstance());
        assertEquals(EMPTY, bean);
    }
}
