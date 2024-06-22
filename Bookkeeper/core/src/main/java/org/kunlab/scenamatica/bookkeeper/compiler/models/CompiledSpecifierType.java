package org.kunlab.scenamatica.bookkeeper.compiler.models;

import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;
import org.objectweb.asm.Type;

import java.util.Map;

public class CompiledSpecifierType extends CompiledType implements IPrimitiveType
{
    public static final String CLASS_ENTITY = "org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier";
    public static final TypeReference REF_ENTITY = new TypeReference(
            "entitySpecifier",
            new CompiledSpecifierType(
                    "entity",
                    CLASS_ENTITY
            )
    );

    public static final String CLASS_PLAYER = "org.kunlab.scenamatica.interfaces.structures.specifiers.PlayerSpecifier";
    public static final TypeReference REF_PLAYER = new TypeReference(
            "playerSpecifier",
            new CompiledSpecifierType(
                    "player",
                    CLASS_PLAYER
            )
    );

    private CompiledSpecifierType(String type, String className)
    {
        super(
                type + "Specifier",
                type.substring(0, 1).toUpperCase() + type.substring(1) + "Specifier",
                null,
                className
        );
    }

    @Override
    public Map<String, Object> serialize()
    {
        Map<String, Object> map = super.serialize();
        map.put(KEY_TYPE, "specifier");

        return map;
    }

    public static boolean isEntitySpecifier(Type type)
    {
        return type.getClassName().equals(CLASS_ENTITY);
    }

    public static boolean isPlayerSpecifier(Type type)
    {
        return type.getClassName().equals(CLASS_PLAYER);
    }
}
