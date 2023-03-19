package net.kunmc.lab.scenamatica.scenariofile.beans.trigger;

import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ActionBeanSerializeTest;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ScenarioBeanSerializeTest;
import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TriggerBeanSerializeTest
{
    public static final TriggerBean FULFILLED = new TriggerBean(
            TriggerType.ON_ACTION,
            ActionBeanSerializeTest.FULFILLED,
            Arrays.asList(ScenarioBeanSerializeTest.FULFILLED, ScenarioBeanSerializeTest.FULFILLED),
            Arrays.asList(ScenarioBeanSerializeTest.FULFILLED, ScenarioBeanSerializeTest.FULFILLED)
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(ActionBeanSerializeTest.FULFILLED_MAP)
    {{
        this.put("type", "action");
        this.put("before", Arrays.asList(ScenarioBeanSerializeTest.FULFILLED_MAP, ScenarioBeanSerializeTest.FULFILLED_MAP));
        this.put("after", Arrays.asList(ScenarioBeanSerializeTest.FULFILLED_MAP, ScenarioBeanSerializeTest.FULFILLED_MAP));
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = TriggerBean.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        TriggerBean bean = TriggerBean.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, bean);
    }
}
