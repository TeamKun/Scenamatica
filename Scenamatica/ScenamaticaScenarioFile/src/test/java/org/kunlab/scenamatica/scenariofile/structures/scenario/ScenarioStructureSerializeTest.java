package org.kunlab.scenamatica.scenariofile.structures.scenario;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.structures.scenario.ScenarioStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ScenarioStructureSerializeTest
{
    public static final ScenarioStructure FULFILLED = new ScenarioStructureImpl(
            ScenarioType.ACTION_EXECUTE,
            ActionStructureSerializeTest.FULFILLED,
            "NONAME",
            null,
            114514L
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(ActionStructureSerializeTest.FULFILLED_MAP)
    {{
        this.put("type", "execute");
        this.put("timeout", 114514L);
        this.put("name", "NONAME");
    }};

    public static final ScenarioStructure EMPTY = new ScenarioStructureImpl(
            ScenarioType.ACTION_EXECUTE,
            ActionStructureSerializeTest.EMPTY,
            null,
            null,
            -1
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>(ActionStructureSerializeTest.EMPTY_MAP)
    {{
        this.put("type", "execute");
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = ScenarioStructureImpl.serialize(FULFILLED, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ScenarioStructure structure = ScenarioStructureImpl.deserialize(FULFILLED_MAP, StructureSerializerImpl.getInstance());

        assertEquals(FULFILLED, structure);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = ScenarioStructureImpl.serialize(EMPTY, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ScenarioStructure structure = ScenarioStructureImpl.deserialize(EMPTY_MAP, StructureSerializerImpl.getInstance());

        ScenarioStructure expected = new ScenarioStructureImpl(
                ScenarioType.ACTION_EXECUTE,
                ActionStructureSerializeTest.EMPTY,
                null,
                null,
                100  // デフォルト値で補完される
        );

        assertEquals(expected, structure);
    }
}
