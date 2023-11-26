package org.kunlab.scenamatica.scenariofile.beans.entity;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileBean;
import org.kunlab.scenamatica.scenariofile.BeanSerializerImpl;
import org.kunlab.scenamatica.scenariofile.beans.entity.entities.ProjectileBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.utils.MapTestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectileBeanSerializeTest
{

    public static final ProjectileBean FULFILLED = new ProjectileBeanImpl(
            EntityBeanSerializeTest.FULFILLED,
            EntityBeanSerializeTest.FULFILLED,
            true
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(EntityBeanSerializeTest.FULFILLED_MAP)
    {{
        this.put(ProjectileBean.KEY_SHOOTER, EntityBeanSerializeTest.FULFILLED_MAP);
        this.put(ProjectileBean.KEY_DOES_BOUNCE, true);
    }};

    public static final ProjectileBean EMPTY = new ProjectileBeanImpl(
            EntityBeanSerializeTest.EMPTY,
            null,
            null
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<>(EntityBeanSerializeTest.EMPTY_MAP);

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = ProjectileBeanImpl.serialize(FULFILLED, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ProjectileBean bean = ProjectileBeanImpl.deserialize(FULFILLED_MAP, BeanSerializerImpl.getInstance());

        assertEquals(FULFILLED, bean);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = ProjectileBeanImpl.serialize(EMPTY, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ProjectileBean bean = ProjectileBeanImpl.deserialize(EMPTY_MAP, BeanSerializerImpl.getInstance());

        assertEquals(EMPTY, bean);
    }
}
