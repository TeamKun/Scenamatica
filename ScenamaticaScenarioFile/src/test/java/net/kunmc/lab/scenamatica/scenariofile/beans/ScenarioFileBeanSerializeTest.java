package net.kunmc.lab.scenamatica.scenariofile.beans;

import net.kunmc.lab.peyangpaperutils.versioning.Version;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScenarioFileBeanSerializeTest
{
    public static final ScenarioFileBean FULFILLED = new ScenarioFileBeanImpl(
            Version.of("0.1.0"),
            "eveything",
            "A scenario that does everything",
            114514L,
            0,
            Arrays.asList(
                    TriggerBeanSerializeTest.FULFILLED,
                    TriggerBeanSerializeTest.FULFILLED,
                    TriggerBeanSerializeTest.FULFILLED
            ),
            null,
            ContextBeanSerializeTest.FULFILLED,
            Arrays.asList(
                    ScenarioBeanSerializeTest.FULFILLED,
                    ScenarioBeanSerializeTest.FULFILLED
            )
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("scenamatica", "0.1.0");
        this.put("name", "eveything");
        this.put("description", "A scenario that does everything");
        this.put("timeout", 114514L);
        this.put("order", 0);
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
            Version.of("0.1.0"),
            "nothing",
            "A scenario that does nothing",
            ScenarioFileBeanImpl.DEFAULT_TIMEOUT_TICK,
            ScenarioFileBeanImpl.DEFAULT_ORDER,
            Collections.emptyList(),
            null,
            null,
            Collections.emptyList()
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        this.put("scenamatica", "0.1.0");
        this.put("name", "nothing");
        this.put("description", "A scenario that does nothing");
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

    @Test
    void バージョンが不正な場合は例外が発生するか()
    {
        Map<String, Object> map = new HashMap<>(FULFILLED_MAP);
        map.put("scenamatica", "awdawdawd");

        assertThrows(IllegalArgumentException.class, () -> ScenarioFileBeanImpl.deserialize(map));
    }
}
