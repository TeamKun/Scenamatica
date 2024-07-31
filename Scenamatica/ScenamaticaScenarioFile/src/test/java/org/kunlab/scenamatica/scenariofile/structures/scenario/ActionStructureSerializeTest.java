package org.kunlab.scenamatica.scenariofile.structures.scenario;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.structures.scenario.ActionStructure;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ActionStructureSerializeTest
{
    public static final ActionStructure FULFILLED = new ActionStructureImpl(
            "none",
            null
    );

    public static final HashMap<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("action", "none");
    }};

    public static final ActionStructure EMPTY = new ActionStructureImpl(
            "none",
            null
    );

    public static final HashMap<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        this.put("action", "none");
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = ActionStructureImpl.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ActionStructure structure = ActionStructureImpl.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, structure);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = ActionStructureImpl.serialize(EMPTY);

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ActionStructure structure = ActionStructureImpl.deserialize(EMPTY_MAP);

        assertEquals(EMPTY, structure);
    }
}
