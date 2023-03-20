package net.kunmc.lab.scenamatica.scenariofile.beans;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.ScenarioFileBean;
import net.kunmc.lab.scenamatica.scenariofile.ScenarioFileBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.context.ContextBeanSerializeTest;
import net.kunmc.lab.scenamatica.scenariofile.beans.scenario.ScenarioBeanSerializeTest;
import net.kunmc.lab.scenamatica.scenariofile.beans.trigger.TriggerBeanSerializeTest;
import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScenarioFileBeanSerializeTest
{
    public static final ScenarioFileBean FULFILLED = new ScenarioFileBeanImpl(
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
        this.put("name", "A scenario that does everything");
        this.put("on", Arrays.asList(
                TriggerBeanSerializeTest.FULFILLED_MAP,
                TriggerBeanSerializeTest.FULFILLED_MAP,
                TriggerBeanSerializeTest.FULFILLED_MAP
        ));
        this.put("context", ContextBeanSerializeTest.FULFILLED_MAP);
        this.put("scenario", Arrays.asList(
                ScenarioBeanSerializeTest.FULFILLED_MAP,
                ScenarioBeanSerializeTest.FULFILLED_MAP
        ));
    }};

    public static final ScenarioFileBeanImpl EMPTY = new ScenarioFileBeanImpl(
            "A scenario that does nothing",
            Collections.emptyList(),
            null,
            Collections.emptyList()
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        this.put("name", "A scenario that does nothing");
        this.put("on", Collections.emptyList());
        this.put("scenario", Collections.emptyList());
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = ScenarioFileBeanImpl.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ScenarioFileBean bean = ScenarioFileBeanImpl.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, bean);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = ScenarioFileBeanImpl.serialize(EMPTY);

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ScenarioFileBean bean = ScenarioFileBeanImpl.deserialize(EMPTY_MAP);

        assertEquals(EMPTY, bean);
    }
}
