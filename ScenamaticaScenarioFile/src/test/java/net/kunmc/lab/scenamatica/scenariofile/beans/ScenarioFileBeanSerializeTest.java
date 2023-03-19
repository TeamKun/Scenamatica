package net.kunmc.lab.scenamatica.scenariofile.beans;

import net.kunmc.lab.scenamatica.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.context.ContextBeanSerializeTest;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ScenarioBeanSerializeTest;
import net.kunmc.lab.scenamatica.scenariofile.beans.trigger.TriggerBeanSerializeTest;
import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScenarioFileBeanSerializeTest
{
    public static final ScenarioFileBean FULFILLED = new ScenarioFileBean(
            "A scenario that does everything",
            Arrays.asList(
                    TriggerBeanSerializeTest.FULFILLED,
                    TriggerBeanSerializeTest.FULFILLED,
                    TriggerBeanSerializeTest.FULFILLED
            ),
            ContextBeanSerializeTest.FULFILLED,
            Arrays.asList(
                    ScenarioBeanSerializeTest.FULFILLED,
                    ScenarioBeanSerializeTest.FULFILLED
            )
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        put("name", "A scenario that does everything");
        put("on", Arrays.asList(
                TriggerBeanSerializeTest.FULFILLED_MAP,
                TriggerBeanSerializeTest.FULFILLED_MAP,
                TriggerBeanSerializeTest.FULFILLED_MAP
        ));
        put("context", ContextBeanSerializeTest.FULFILLED_MAP);
        put("scenario", Arrays.asList(
                ScenarioBeanSerializeTest.FULFILLED_MAP,
                ScenarioBeanSerializeTest.FULFILLED_MAP
        ));
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = ScenarioFileBean.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ScenarioFileBean bean = ScenarioFileBean.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, bean);
    }

}
