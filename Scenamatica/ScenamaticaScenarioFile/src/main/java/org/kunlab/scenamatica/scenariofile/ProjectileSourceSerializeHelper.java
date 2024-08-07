package org.kunlab.scenamatica.scenariofile;

import org.bukkit.entity.Entity;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.ProjectileSourceStructure;
import org.kunlab.scenamatica.structures.minecraft.misc.BlockStructureImpl;

import java.util.Map;

public class ProjectileSourceSerializeHelper
{
    public static String KEY_KIND = "kind";

    public static String KIND_ENTITY = "entity";
    public static String KIND_BLOCK = "block";

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
        if (KIND_ENTITY.equalsIgnoreCase((String) map.get(KEY_KIND)))
            return tryDeserialize(map, serializer, EntityStructure.class);
        else if (KIND_BLOCK.equalsIgnoreCase((String) map.get(KEY_KIND)))
            return tryDeserialize(map, serializer, BlockStructure.class);

        // 自動推論

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
        if (KIND_ENTITY.equalsIgnoreCase((String) map.get(KEY_KIND)))
            serializer.validate(map, EntityStructure.class);
        else if (KIND_BLOCK.equalsIgnoreCase((String) map.get(KEY_KIND)))
            serializer.validate(map, BlockStructure.class);
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

    public static boolean isApplicable(Object o)
    {
        return o instanceof ProjectileSource;
    }
}
