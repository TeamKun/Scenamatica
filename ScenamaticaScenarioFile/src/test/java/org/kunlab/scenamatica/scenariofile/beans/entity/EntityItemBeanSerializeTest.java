package org.kunlab.scenamatica.scenariofile.beans.entity;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityBean;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.EntityItemBean;
import org.kunlab.scenamatica.scenariofile.BeanSerializerImpl;
import org.kunlab.scenamatica.scenariofile.beans.entity.entities.EntityItemBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.inventory.ItemStackBeanSerializeTest;
import org.kunlab.scenamatica.scenariofile.beans.utils.MapTestUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityItemBeanSerializeTest
{
    public static final EntityItemBean EMPTY = new EntityItemBeanImpl(
            EntityBeanSerializeTest.EMPTY,
            ItemStackBeanSerializeTest.EMPTY,
            null,
            null,
            null,
            null,
            null
    );
    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        this.putAll(EntityBeanSerializeTest.EMPTY_MAP);
        this.putAll(ItemStackBeanSerializeTest.EMPTY_MAP);
    }};
    private static final UUID FULLFILLED_OWNER_UUID = UUID.fromString("20b161fb-9956-407d-a52a-bb981c11f5c0");
    private static final UUID FULLFILLED_THROWER_UUID = UUID.fromString("4e11d4a6-4624-475f-8be0-d9e984b047f4");
    public static final EntityItemBean FULFILLED = new EntityItemBeanImpl(
            EntityBeanSerializeTest.FULFILLED,
            ItemStackBeanSerializeTest.FULFILLED,
            1,
            FULLFILLED_OWNER_UUID,
            FULLFILLED_THROWER_UUID,
            false,
            false
    );
    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.putAll(EntityBeanSerializeTest.FULFILLED_MAP);
        this.putAll(ItemStackBeanSerializeTest.FULFILLED_MAP);

        this.put(EntityItemBean.KEY_PICKUP_DELAY, 1);
        this.put(EntityItemBean.KEY_OWNER, FULLFILLED_OWNER_UUID.toString());
        this.put(EntityItemBean.KEY_THROWER, FULLFILLED_THROWER_UUID.toString());
        this.put(EntityItemBean.KEY_CAN_MOB_PICKUP, false);
        this.put(EntityItemBean.KEY_WILL_AGE, false);
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = EntityItemBeanImpl.serialize(FULFILLED, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        EntityBean entity = EntityItemBeanImpl.deserialize(FULFILLED_MAP, BeanSerializerImpl.getInstance());

        assertEquals(FULFILLED, entity);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = EntityItemBeanImpl.serialize(EMPTY, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        EntityBean entity = EntityItemBeanImpl.deserialize(EMPTY_MAP, BeanSerializerImpl.getInstance());

        assertEquals(EMPTY, entity);
    }
}
