package org.kunlab.scenamatica.scenariofile.structures.entity;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.entities.EntityItemStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.inventory.ItemStackStructureSerializeTest;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityItemStructureSerializeTest
{
    public static final EntityItemStructure EMPTY = new EntityItemStructureImpl(
            AEntityStructureSerializeTest.EMPTY,
            ItemStackStructureSerializeTest.EMPTY,
            null,
            null,
            null,
            null,
            null
    );
    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        this.putAll(AEntityStructureSerializeTest.EMPTY_MAP);
        this.putAll(ItemStackStructureSerializeTest.EMPTY_MAP);
    }};
    private static final UUID FULLFILLED_OWNER_UUID = UUID.fromString("20b161fb-9956-407d-a52a-bb981c11f5c0");
    private static final UUID FULLFILLED_THROWER_UUID = UUID.fromString("4e11d4a6-4624-475f-8be0-d9e984b047f4");
    public static final EntityItemStructure FULFILLED = new EntityItemStructureImpl(
            AEntityStructureSerializeTest.FULFILLED,
            ItemStackStructureSerializeTest.FULFILLED,
            1,
            FULLFILLED_OWNER_UUID,
            FULLFILLED_THROWER_UUID,
            false,
            false
    );
    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.putAll(AEntityStructureSerializeTest.FULFILLED_MAP);
        this.putAll(ItemStackStructureSerializeTest.FULFILLED_MAP);

        this.put(EntityItemStructure.KEY_PICKUP_DELAY, 1);
        this.put(EntityItemStructure.KEY_OWNER, FULLFILLED_OWNER_UUID.toString());
        this.put(EntityItemStructure.KEY_THROWER, FULLFILLED_THROWER_UUID.toString());
        this.put(EntityItemStructure.KEY_CAN_MOB_PICKUP, false);
        this.put(EntityItemStructure.KEY_WILL_AGE, false);
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = EntityItemStructureImpl.serialize(FULFILLED, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        EntityStructure entity = EntityItemStructureImpl.deserialize(FULFILLED_MAP, StructureSerializerImpl.getInstance());

        assertEquals(FULFILLED, entity);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = EntityItemStructureImpl.serialize(EMPTY, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        EntityStructure entity = EntityItemStructureImpl.deserialize(EMPTY_MAP, StructureSerializerImpl.getInstance());

        assertEquals(EMPTY, entity);
    }
}
