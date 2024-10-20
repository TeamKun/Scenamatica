package org.kunlab.scenamatica.structures;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;

import java.util.Locale;

public class StructureMappers
{
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
