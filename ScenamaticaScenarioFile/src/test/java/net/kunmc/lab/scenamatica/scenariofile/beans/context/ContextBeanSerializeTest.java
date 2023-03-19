package net.kunmc.lab.scenamatica.scenariofile.beans.context;

import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextBeanSerializeTest
{
    public static final ContextBean FULFILLED = new ContextBean(
            Arrays.asList(
                    PlayerBeanSerializeTest.FULFILLED,
                    PlayerBeanSerializeTest.FULFILLED,
                    PlayerBeanSerializeTest.FULFILLED
            ),
            WorldBeanSerializeTest.FULFILLED
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        put("pseudoPlayers", Arrays.asList(
                PlayerBeanSerializeTest.FULFILLED_MAP,
                PlayerBeanSerializeTest.FULFILLED_MAP,
                PlayerBeanSerializeTest.FULFILLED_MAP
        ));
        put("world", WorldBeanSerializeTest.FULFILLED_MAP);
    }};

    public static final ContextBean EMPTY = new ContextBean(
            null,
            null
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        put("pseudoPlayers", null);
        put("world", null);
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = ContextBean.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ContextBean bean = ContextBean.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, bean);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = ContextBean.serialize(EMPTY);
        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ContextBean bean = ContextBean.deserialize(EMPTY_MAP);
        assertEquals(EMPTY, bean);
    }
}
