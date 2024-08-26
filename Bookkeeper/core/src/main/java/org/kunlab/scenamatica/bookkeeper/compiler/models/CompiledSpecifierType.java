package org.kunlab.scenamatica.bookkeeper.compiler.models;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.compiler.SerializingContext;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.HashMap;
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
                null,
                className,
                null,
                null
        );
    }

    @Override
    public Map<String, Object> serialize(@NotNull SerializingContext ctxt)
    {
        Map<String, Object> map = super.serialize(ctxt);
        if (ctxt.isJSONSchema())
        {
            map.remove(KEY_TYPE);
            map.put("anyOf", new ArrayList<Map<String, Object>>()
            {{
                this.add(new HashMap<String, Object>()
                {{
                    this.put(KEY_TYPE, "string");
                }});
                this.add(new HashMap<String, Object>()
                {{
                    this.put(KEY_TYPE, "object");
                    this.put("additionalProperties", true);
                }});
            }});
        }
        else
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
