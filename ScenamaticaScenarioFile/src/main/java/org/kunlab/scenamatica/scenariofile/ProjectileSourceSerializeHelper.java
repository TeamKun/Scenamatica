package org.kunlab.scenamatica.scenariofile;

import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.ProjectileSourceStructure;

import java.util.Map;

public class ProjectileSourceSerializeHelper
{
    public static Map<String, Object> serialize(ProjectileSourceStructure structure, StructureSerializer serializer)
    {
        if (structure instanceof EntityStructure)
            return serializer.serialize((EntityStructure) structure, EntityStructure.class);
        else if (structure instanceof BlockStructure)
            return serializer.serialize((BlockStructure) structure, BlockStructure.class);
        else
            throw new IllegalArgumentException("Unrecognized ProjectileSourceStructure: " + structure);
    }

    public static ProjectileSourceStructure deserialize(Map<String, Object> map, StructureSerializer serializer)
    {
        ProjectileSourceStructure structure = tryDeserialize(map, serializer, EntityStructure.class);
        if (structure != null)
            return structure;

        structure = tryDeserialize(map, serializer, BlockStructure.class);
        if (structure != null)
            return structure;

        throw new IllegalArgumentException("Unrecognized ProjectileSourceStructure: " + map);
    }

    public static void validate(Map<String, Object> map, StructureSerializer serializer)
    {
        RuntimeException e = tryValidate(map, serializer, EntityStructure.class);
        if (e == null)
            e = tryValidate(map, serializer, BlockStructure.class);

        if (e != null)
            throw e;
    }

    private static <T extends Structure> T tryDeserialize(Map<String, Object> map, StructureSerializer serializer, Class<T> clazz)
    {
        try
        {
            return serializer.deserialize(map, clazz);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private static RuntimeException tryValidate(Map<String, Object> map, StructureSerializer serializer, Class<? extends Structure> clazz)
    {
        try
        {
            serializer.validate(map, clazz);
            return null;
        }
        catch (RuntimeException e)
        {
            if (e instanceof NullPointerException)
                return null;
            return e;
        }
    }
}
