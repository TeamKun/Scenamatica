package org.kunlab.scenamatica.structures;

import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;

import java.util.Locale;

public class StructureValidators
{
    public static final StructuredYamlNode.Validator MATERIAL_NAME = node -> {
        String mayMaterialName = node.asString();
        Material foundMaterial = Utils.searchMaterial(mayMaterialName);
        if (foundMaterial == null)
            throw new IllegalArgumentException("Material-like expected, but not found: " + mayMaterialName);

        return null;
    };

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
}
