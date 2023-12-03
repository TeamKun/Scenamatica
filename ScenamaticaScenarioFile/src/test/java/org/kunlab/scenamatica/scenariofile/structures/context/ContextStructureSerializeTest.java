package org.kunlab.scenamatica.scenariofile.structures.context;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.context.ContextStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.AEntityStructureSerializeTest;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ContextStructureSerializeTest
{
    public static final ContextStructureImpl FULFILLED = new ContextStructureImpl(
            Arrays.asList(
                    PlayerStructureSerializeTest.FULFILLED,
                    PlayerStructureSerializeTest.FULFILLED,
                    PlayerStructureSerializeTest.FULFILLED
            ),
            Arrays.asList(
                    AEntityStructureSerializeTest.FULFILLED,
                    AEntityStructureSerializeTest.FULFILLED,
                    AEntityStructureSerializeTest.FULFILLED
            ),
            StageStructureSerializeTest.FULFILLED
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("actors", Arrays.asList(
                PlayerStructureSerializeTest.FULFILLED_MAP,
                PlayerStructureSerializeTest.FULFILLED_MAP,
                PlayerStructureSerializeTest.FULFILLED_MAP
        ));
        this.put("entities", Arrays.asList(
                AEntityStructureSerializeTest.FULFILLED_MAP,
                AEntityStructureSerializeTest.FULFILLED_MAP,
                AEntityStructureSerializeTest.FULFILLED_MAP
        ));
        this.put("stage", StageStructureSerializeTest.FULFILLED_MAP);
    }};

    public static final ContextStructureImpl EMPTY = new ContextStructureImpl(
            Collections.emptyList(),
            Collections.emptyList(),
            null
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = ContextStructureImpl.serialize(FULFILLED, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ContextStructure structure = ContextStructureImpl.deserialize(FULFILLED_MAP, StructureSerializerImpl.getInstance());

        assertEquals(FULFILLED, structure);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = ContextStructureImpl.serialize(EMPTY, StructureSerializerImpl.getInstance());
        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ContextStructure structure = ContextStructureImpl.deserialize(EMPTY_MAP, StructureSerializerImpl.getInstance());
        assertEquals(EMPTY, structure);
    }
}
