package org.kunlab.scenamatica.structures;

import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;

import java.util.Locale;
import java.util.regex.Pattern;

public class StructureValidators
{
    public static final StructuredYamlNode.Validator MATERIAL_NAME = node -> {
        String mayMaterialName = node.asString();
        Material foundMaterial = Utils.searchMaterial(mayMaterialName);
        if (foundMaterial == null)
            throw new IllegalArgumentException("Material-like expected, but not found: " + mayMaterialName);

        return null;
    };
    public static final StructuredYamlNode.Validator UUID = UUIDValidator::validate;

    @NotNull
    public static StructuredYamlNode.Validator enumName(@NotNull Class<? extends Enum<?>> enumClass)
    {
        return node -> {
            boolean found = false;
            String mayEnumName = node.asString().toUpperCase(Locale.ENGLISH);
            for (Enum<?> enumConstant : enumClass.getEnumConstants())
            {
                if (enumConstant.name().equalsIgnoreCase(mayEnumName))
                {
                    found = true;
                    break;
                }
            }

            if (!found)
                throw new IllegalArgumentException("Value is not a valid enum name of: " + enumClass.getSimpleName());
            return null;
        };
    }

    @NotNull
    public static StructuredYamlNode.Validator ranged(@Nullable Number min, @Nullable Number max)
    {
        if (min == null && max == null)
            throw new IllegalArgumentException("Both min and max are null");

        if (!(min == null || max == null || min.doubleValue() <= max.doubleValue()))
            throw new IllegalArgumentException(String.format("Min value(%s) is greater than max value(%s)", min, max));

        return node -> {
            Number value = node.asDouble();
            boolean isValid = (min == null || value.doubleValue() >= min.doubleValue())
                    && (max == null || value.doubleValue() <= max.doubleValue());

            if (!isValid)
                throw new IllegalArgumentException("Value must be in range: " + (min == null ? "": min) + " ~ " + (max == null ? "": max));

            return null;
        };
    }

    @NotNull
    public static StructuredYamlNode.Validator mapType(@NotNull StructuredYamlNode.Validator keyValidator, @NotNull StructuredYamlNode.Validator valueValidator)
    {
        return node -> {
            if (!node.isType(YAMLNodeType.MAPPING))
                throw new IllegalArgumentException("Value must be a mapping");

            for (Pair<? extends StructuredYamlNode, ? extends StructuredYamlNode> child : node.getMappingEntries())
            {
                keyValidator.validate(child.getLeft());
                valueValidator.validate(child.getRight());
            }

            return null;
        };
    }

    public static StructuredYamlNode.Validator listType(@NotNull StructureSerializer serializer, @NotNull Class<? extends Structure> clazz)
    {
        return node -> {
            if (!node.isType(YAMLNodeType.LIST))
                throw new IllegalArgumentException("Value must be a list");

            for (StructuredYamlNode element : node.asList())
                serializer.validate(element, clazz);

            return null;
        };
    }

    private static class UUIDValidator
    {
        private static final Pattern PATTERN_FULL_QUALIFIED_UUID = Pattern.compile("[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}");
        private static final Pattern PATTERN_COMPACT_UUID = Pattern.compile("[0-9a-fA-F]{32}");

        public static Object validate(StructuredYamlNode node)
        {
            String uuidString = node.asString();
            if (PATTERN_FULL_QUALIFIED_UUID.matcher(uuidString).matches() || PATTERN_COMPACT_UUID.matcher(uuidString).matches())
                return null;

            throw new IllegalArgumentException("Value is not a valid UUID");
        }
    }
}
