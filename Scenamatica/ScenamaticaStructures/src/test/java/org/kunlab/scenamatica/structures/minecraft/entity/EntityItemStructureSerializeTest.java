package org.kunlab.scenamatica.structures.minecraft.entity;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.EntityItemStructure;
import org.kunlab.scenamatica.structures.minecraft.StructureSerializerMock;
import org.kunlab.scenamatica.structures.minecraft.entity.entities.EntityItemStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.inventory.ItemStackStructureSerializeTest;
import org.kunlab.scenamatica.structures.minecraft.utils.MapTestUtil;
import org.kunlab.scenamatica.structures.specifiers.EntitySpecifierImpl;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityItemStructureSerializeTest
{
    public static final EntityItemStructure EMPTY = new EntityItemStructureImpl(
            EntityStructureSerializeTest.EMPTY,
            ItemStackStructureSerializeTest.EMPTY,
            null,
            EntitySpecifierImpl.EMPTY,
            EntitySpecifierImpl.EMPTY,
            null,
            null
    );
    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        this.putAll(EntityStructureSerializeTest.EMPTY_MAP);
        this.putAll(ItemStackStructureSerializeTest.EMPTY_MAP);
    }};
    private static final UUID FULLFILLED_OWNER_UUID = UUID.fromString("20b161fb-9956-407d-a52a-bb981c11f5c0");
    private static final UUID FULLFILLED_THROWER_UUID = UUID.fromString("4e11d4a6-4624-475f-8be0-d9e984b047f4");
    public static final EntityItemStructure FULFILLED = new EntityItemStructureImpl(
            EntityStructureSerializeTest.FULFILLED,
            ItemStackStructureSerializeTest.FULFILLED,
            1,
            EntitySpecifierImpl.of(FULLFILLED_OWNER_UUID),
            EntitySpecifierImpl.of(FULLFILLED_THROWER_UUID),
            false,
            false
    );
    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.putAll(EntityStructureSerializeTest.FULFILLED_MAP);
        this.putAll(ItemStackStructureSerializeTest.FULFILLED_MAP);

        this.put(EntityItemStructure.KEY_PICKUP_DELAY, 1);
        this.put(EntityItemStructure.KEY_OWNER, FULLFILLED_OWNER_UUID);
        this.put(EntityItemStructure.KEY_THROWER, FULLFILLED_THROWER_UUID);
        this.put(EntityItemStructure.KEY_CAN_MOB_PICKUP, false);
        this.put(EntityItemStructure.KEY_WILL_AGE, false);
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = EntityItemStructureImpl.serializeItem(FULFILLED, StructureSerializerMock.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        EntityStructure entity = EntityItemStructureImpl.deserializeItem(FULFILLED_MAP, StructureSerializerMock.getInstance());

        assertEquals(FULFILLED, entity);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = EntityItemStructureImpl.serializeItem(EMPTY, StructureSerializerMock.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        EntityStructure entity = EntityItemStructureImpl.deserializeItem(EMPTY_MAP, StructureSerializerMock.getInstance());

        assertEquals(EMPTY, entity);
    }
}
