package org.kunlab.scenamatica.scenariofile.structures.entity;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.entities.ProjectileStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ProjectileStructureSerializeTest
{

    public static final ProjectileStructure FULFILLED = new ProjectileStructureImpl(
            AEntityStructureSerializeTest.FULFILLED,
            AEntityStructureSerializeTest.FULFILLED,
            true
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(AEntityStructureSerializeTest.FULFILLED_MAP)
    {{
        this.put(ProjectileStructure.KEY_SHOOTER, AEntityStructureSerializeTest.FULFILLED_MAP);
        this.put(ProjectileStructure.KEY_DOES_BOUNCE, true);
    }};

    public static final ProjectileStructure EMPTY = new ProjectileStructureImpl(
            AEntityStructureSerializeTest.EMPTY,
            null,
            null
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<>(AEntityStructureSerializeTest.EMPTY_MAP);

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = ProjectileStructureImpl.serialize(FULFILLED, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        ProjectileStructure structure = ProjectileStructureImpl.deserialize(FULFILLED_MAP, StructureSerializerImpl.getInstance());

        assertEquals(FULFILLED, structure);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = ProjectileStructureImpl.serialize(EMPTY, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ProjectileStructure structure = ProjectileStructureImpl.deserialize(EMPTY_MAP, StructureSerializerImpl.getInstance());

        assertEquals(EMPTY, structure);
    }
}
