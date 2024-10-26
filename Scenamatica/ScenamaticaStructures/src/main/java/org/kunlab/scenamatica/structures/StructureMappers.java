package org.kunlab.scenamatica.structures;

import com.destroystokyo.paper.Namespaced;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;

import java.util.Locale;
import java.util.UUID;

public class StructureMappers
{
    public static final StructuredYamlNode.ValueMapper<UUID> UU1D = node -> {
        String uuidString = node.asString();
        if (uuidString.contains("-"))
            return UUID.fromString(uuidString);

        if (uuidString.length() != 32)
            throw new IllegalArgumentException("Value is not a valid UUID");

        String repairedUUIDString =
                uuidString.substring(0, 8) + "-" +
                        uuidString.substring(8, 12) + "-" +
                        uuidString.substring(12, 16) + "-" +
                        uuidString.substring(16, 20) + "-" +
                        uuidString.substring(20);

        return UUID.fromString(repairedUUIDString.toLowerCase(Locale.ENGLISH));
    };

    public static final StructuredYamlNode.ValueMapper<Material> MATERIAL_NAME = node -> {
        String mayMaterialName = node.asString();
        Material foundMaterial = Utils.searchMaterial(mayMaterialName);
        if (foundMaterial == null)
            throw new IllegalArgumentException("Material-like expected, but not found: " + mayMaterialName);

        return foundMaterial;
    };

    public static final StructuredYamlNode.ValueMapper<Namespaced> NAMESPACED = node -> {
        String str = node.asString();
        String[] split = str.split(":", 2);
        if (split.length == 1)
            return new NamespacedKey("minecraft", split[0]);
        else
            return new NamespacedKey(split[0], split[1]);
    };

    @NotNull
    public static <T extends Enum<T>> StructuredYamlNode.ValueMapper<T> enumName(@NotNull Class<T> enumClass)
    {
        return node -> {
            String mayEnumName = node.asString().toUpperCase(Locale.ENGLISH);
            for (T enumConstant : enumClass.getEnumConstants())
            {
                if (enumConstant.name().equalsIgnoreCase(mayEnumName))
                    return enumConstant;
            }

            throw new IllegalArgumentException("Value is not a valid enum name of: " + enumClass.getSimpleName());
        };
    }
}
