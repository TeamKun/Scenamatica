package org.kunlab.scenamatica.structures;

import com.destroystokyo.paper.Namespaced;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.exceptions.scenariofile.UnknownEnumValueException;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public class StructureMappers
{
    public static final StructuredYamlNode.ValueMapper<java.util.UUID> UUID = node -> {
        String uuidString = node.asString();
        if (uuidString.contains("-"))
            return java.util.UUID.fromString(uuidString);

        if (uuidString.length() != 32)
            throw new IllegalArgumentException("Value is not a valid UUID");

        String repairedUUIDString =
                uuidString.substring(0, 8) + "-" +
                        uuidString.substring(8, 12) + "-" +
                        uuidString.substring(12, 16) + "-" +
                        uuidString.substring(16, 20) + "-" +
                        uuidString.substring(20);

        return java.util.UUID.fromString(repairedUUIDString.toLowerCase(Locale.ENGLISH));
    };

    public static final StructuredYamlNode.ValueMapper<Material> MATERIAL_NAME = node -> {
        String mayMaterialName = node.asString();
        Material foundMaterial = Utils.searchMaterial(mayMaterialName);
        if (foundMaterial == null)
            throw new IllegalArgumentException("Material-like expected, but not found: " + mayMaterialName);

        return foundMaterial;
    };

    public static final StructuredYamlNode.ValueMapper<NamespacedKey> NAMESPACED_KEY = node -> {
        String str = node.asString();
        String[] split = str.split(":", 2);
        if (split.length == 1)
            return new NamespacedKey("minecraft", split[0]);
        else
            return new NamespacedKey(split[0], split[1]);
    };

    public static final StructuredYamlNode.ValueMapper<Namespaced> NAMESPACED = node -> (Namespaced) NAMESPACED_KEY.map(node);

    @NotNull
    public static <T extends Enum<T>> StructuredYamlNode.ValueMapper<T> enumName(@NotNull Class<T> enumClass, @NotNull Function<T, String> identifier)
    {
        return node -> {

            String enumName = node.asString();
            for (T value : enumClass.getEnumConstants())
            {
                if (identifier.apply(value).equalsIgnoreCase(enumName))
                    return value;
            }

            throw new UnknownEnumValueException(
                    "Value is not a valid enum value",
                    node.getFileName(),
                    node.getKeyName(),
                    node.getStartLine(),
                    node.getLinesOfFile(node.getStartLine()),
                    enumName,
                    enumClass,
                    identifier
            );
        };
    }

    @NotNull
    public static <T extends Enum<T>> StructuredYamlNode.ValueMapper<T> enumName(@NotNull Class<T> enumClass)
    {
        return enumName(enumClass, Enum::name);
    }

    public static <T extends Structure> StructuredYamlNode.ValueMapper<List<T>> deserializedList(@NotNull StructureSerializer serializer, Class<T> elementClass)
    {
        return node -> {
            List<T> list = new ArrayList<>();
            for (StructuredYamlNode elementNode : node.asList())
                list.add(serializer.deserialize(elementNode, elementClass));

            return list;
        };
    }
}
