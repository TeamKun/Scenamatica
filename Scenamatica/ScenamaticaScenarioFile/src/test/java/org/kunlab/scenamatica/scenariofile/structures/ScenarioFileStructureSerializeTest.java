package org.kunlab.scenamatica.scenariofile.structures;

import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.enums.ScenarioOrder;
import org.kunlab.scenamatica.interfaces.scenariofile.ScenarioFileStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;
import org.kunlab.scenamatica.scenariofile.structures.scenario.ScenarioStructureSerializeTest;
import org.kunlab.scenamatica.scenariofile.structures.trigger.TriggerStructureSerializeTest;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ScenarioFileStructureSerializeTest
{
    public static final ScenarioFileStructure FULFILLED = new ScenarioFileStructureImpl(
            Version.of("0.1.0"),
            VersionRangeSerializeTest.FULLFILLED,
            "eveything",
            "A scenario that does everything",
            114514L,
            128,
            Arrays.asList(
                    TriggerStructureSerializeTest.FULFILLED,
                    TriggerStructureSerializeTest.FULFILLED,
                    TriggerStructureSerializeTest.FULFILLED
            ),
            null,
            null,
            Arrays.asList(
                    ScenarioStructureSerializeTest.FULFILLED,
                    ScenarioStructureSerializeTest.FULFILLED
            )
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("scenamatica", "0.1.0");
        this.put("minecraft", VersionRangeSerializeTest.FULLFILLED_MAP);
        this.put("name", "eveything");
        this.put("description", "A scenario that does everything");
        this.put("timeout", 114514L);
        this.put("order", 128);
        this.put("on", Arrays.asList(
                TriggerStructureSerializeTest.FULFILLED_MAP,
                TriggerStructureSerializeTest.FULFILLED_MAP,
                TriggerStructureSerializeTest.FULFILLED_MAP
        ));
        this.put("scenario", Arrays.asList(
                ScenarioStructureSerializeTest.FULFILLED_MAP,
                ScenarioStructureSerializeTest.FULFILLED_MAP
        ));
    }};

    public static final ScenarioFileStructureImpl EMPTY = new ScenarioFileStructureImpl(
            Version.of("0.1.0"),
            null,
            "nothing",
            "A scenario that does nothing",
            ScenarioFileStructureImpl.DEFAULT_TIMEOUT_TICK,
            ScenarioOrder.NORMAL.getOrder(),
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
        Map<String, Object> map = ScenarioFileStructureImpl.serialize(FULFILLED, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ScenarioFileStructure structure = ScenarioFileStructureImpl.deserialize(FULFILLED_MAP, StructureSerializerImpl.getInstance());

        assertEquals(FULFILLED, structure);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = ScenarioFileStructureImpl.serialize(EMPTY, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ScenarioFileStructure structure = ScenarioFileStructureImpl.deserialize(EMPTY_MAP, StructureSerializerImpl.getInstance());

        assertEquals(EMPTY, structure);
    }

    @Test
    void バージョンが不正な場合は例外が発生するか()
    {
        Map<String, Object> map = new HashMap<>(FULFILLED_MAP);
        map.put("scenamatica", "awdawdawd");

        assertThrows(IllegalArgumentException.class, () -> ScenarioFileStructureImpl.deserialize(map, StructureSerializerImpl.getInstance()));
    }
}
