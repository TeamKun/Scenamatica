package org.kunlab.scenamatica.scenariofile.structures.trigger;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.enums.TriggerType;
import org.kunlab.scenamatica.interfaces.structures.trigger.TriggerStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;
import org.kunlab.scenamatica.scenariofile.structures.scenario.ScenarioStructureSerializeTest;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TriggerStructureSerializeTest
{
    public static final TriggerStructure FULFILLED = new TriggerStructureImpl(
            TriggerType.ON_ACTION,
            null,
            Arrays.asList(ScenarioStructureSerializeTest.FULFILLED, ScenarioStructureSerializeTest.FULFILLED),
            Arrays.asList(ScenarioStructureSerializeTest.FULFILLED, ScenarioStructureSerializeTest.FULFILLED),
            null
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("type", "on_action");
        this.put("before", Arrays.asList(ScenarioStructureSerializeTest.FULFILLED_MAP, ScenarioStructureSerializeTest.FULFILLED_MAP));
        this.put("after", Arrays.asList(ScenarioStructureSerializeTest.FULFILLED_MAP, ScenarioStructureSerializeTest.FULFILLED_MAP));
    }};

    public static final TriggerStructure EMPTY = new TriggerStructureImpl(
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
        Map<String, Object> map = TriggerStructureImpl.serialize(FULFILLED, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        TriggerStructure structure = TriggerStructureImpl.deserialize(FULFILLED_MAP, StructureSerializerImpl.getInstance());

        assertEquals(FULFILLED, structure);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = TriggerStructureImpl.serialize(EMPTY, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        TriggerStructure structure = TriggerStructureImpl.deserialize(EMPTY_MAP, StructureSerializerImpl.getInstance());

        assertEquals(EMPTY, structure);
    }
}
