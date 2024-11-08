package org.kunlab.scenamatica.scenariofile;

import org.bukkit.projectiles.ProjectileSource;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.ProjectileSourceStructure;

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

    public static ProjectileSourceStructure deserialize(StructuredYamlNode node, StructureSerializer serializer) throws YamlParsingException
    {
        String kind = node.get(KEY_KIND).asString(null);
        if (KIND_ENTITY.equalsIgnoreCase(kind))
            return tryDeserialize(node, serializer, EntityStructure.class);
        else if (KIND_BLOCK.equalsIgnoreCase(kind))
            return tryDeserialize(node, serializer, BlockStructure.class);

        // 自動推論

        ProjectileSourceStructure structure = tryDeserialize(node, serializer, EntityStructure.class);
        if (structure != null)
            return structure;

        structure = tryDeserialize(node, serializer, BlockStructure.class);
        if (structure != null)
            return structure;

        throw new IllegalArgumentException("Unrecognized ProjectileSourceStructure: " + node);
    }

    public static void validate(StructuredYamlNode node, StructureSerializer serializer) throws YamlParsingException
    {
        String kind = node.get(KEY_KIND).asString(null);
        if (KIND_ENTITY.equalsIgnoreCase(kind))
            serializer.validate(node, EntityStructure.class);
        else if (KIND_BLOCK.equalsIgnoreCase(kind))
            serializer.validate(node, BlockStructure.class);
    }

    private static <T extends Structure> T tryDeserialize(StructuredYamlNode node, StructureSerializer serializer, Class<T> clazz)
    {
        try
        {
            return serializer.deserialize(node, clazz);
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
