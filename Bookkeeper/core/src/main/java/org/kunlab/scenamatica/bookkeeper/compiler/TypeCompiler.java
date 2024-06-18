package org.kunlab.scenamatica.bookkeeper.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.BookkeeperCore;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledSpecifierType;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledStringType;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledType;
import org.kunlab.scenamatica.bookkeeper.compiler.models.IPrimitiveType;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;
import org.kunlab.scenamatica.bookkeeper.definitions.TypeDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.TypePropertyDefinition;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TypeCompiler extends AbstractCompiler<TypeDefinition, CompiledType, TypeReference>
{
    private static final TypeReference PRIMITIVE_BOOLEAN = PrimitiveType.ofRef("boolean", boolean.class);
    private static final TypeReference PRIMITIVE_BYTE = PrimitiveType.ofRef("byte", byte.class);
    private static final TypeReference PRIMITIVE_CHAR = PrimitiveType.ofRef("char", char.class);
    private static final TypeReference PRIMITIVE_SHORT = PrimitiveType.ofRef("short", short.class);
    private static final TypeReference PRIMITIVE_INT = PrimitiveType.ofRef("int", int.class);
    private static final TypeReference PRIMITIVE_LONG = PrimitiveType.ofRef("long", long.class);
    private static final TypeReference PRIMITIVE_FLOAT = PrimitiveType.ofRef("float", float.class);
    private static final TypeReference PRIMITIVE_DOUBLE = PrimitiveType.ofRef("double", double.class);

    private static final TypeReference CO_PRIMITIVE_STRING = PrimitiveType.ofRef("string", String.class);
    private static final TypeReference CO_PRIMITIVE_MAP = PrimitiveType.ofRef("map", Map.class);

    private final BookkeeperCore core;

    public TypeCompiler(BookkeeperCore core)
    {
        super("primitive");
        this.core = core;
    }

    public TypeReference lookup(Type type)
    {
        for (TypeReference reference : this.compiledItemReferences.values())
        {
            CompiledType compiledType = reference.getResolved();
            if (compiledType.getClassName().equals(type.getClassName().replace('.', '/')))
                return reference;
        }

        throw new IllegalArgumentException("Unable to resolve type reference for type '" + type.getClassName() + "'");
    }

    @Override
    protected String toId(CompiledType compiledItem)
    {
        return "";
    }

    @Override
    protected TypeReference doCompile(TypeDefinition definition)
    {
        String id = toId(definition);
        if (this.resolve(id) != null)
            throw new IllegalArgumentException("Type with ID '" + id + "' already exists, consider changing the name of the type.");

        return new TypeReference(
                id,
                new CompiledType(
                        id,
                        definition.name(),
                        definition.clazz().name,
                        definition.mappingOf() != null ? definition.mappingOf().getClassName(): null,
                        compileProperties(definition.properties())
                )
        );
    }

    @Override
    protected String toId(TypeDefinition definition)
    {
        return classNameToId(definition.clazz().name);
    }

    private Map<String, CompiledType.Property> compileProperties(TypePropertyDefinition[] properties)
    {
        if (properties == null)
            return null;

        Map<String, CompiledType.Property> compiledProperties = new HashMap<>();
        for (TypePropertyDefinition property : properties)
        {
            compiledProperties.put(
                    property.name(),
                    new CompiledType.Property(
                            property.name(),
                            resolvePropertyType(property.type()),
                            property.description(),
                            property.required(),
                            property.type().getDescriptor().endsWith("["),
                            property.defaultValue()
                    )
            );
        }

        return compiledProperties;
    }

    @Nullable
    private TypeReference resolveCompiledType(Type asmType)
    {
        for (TypeReference reference : this.compiledItemReferences.values())
        {
            CompiledType compiledType = reference.getResolved();
            if (compiledType.getClassName().equals(asmType.getClassName().replace('.', '/'))
                    || (compiledType.getMappingOf() != null && compiledType.getMappingOf().equals(asmType.getClassName())))
                return reference;
        }

        return null;
    }

    @Override
    public Class<TypeDefinition> getDefinitionType()
    {
        return TypeDefinition.class;
    }

    private TypeReference resolvePropertyType(Type type)
    {
        if (type.getDescriptor().startsWith("["))
            type = type.getElementType(); // 配列は要素型に変換

        String desc = type.getDescriptor();
        if (desc.equals("Ljava/lang/String;"))
            return CO_PRIMITIVE_STRING;
        else if (desc.equals("Ljava/util/Map;"))
            return CO_PRIMITIVE_MAP;
        else if (desc.equals(CompiledStringType.DESC_UUID))
            return CompiledStringType.REF_UUID;
        else if (desc.equals(CompiledStringType.DESC_NAMESPACED))
            return CompiledStringType.REF_NAMESPACED;
        else if (CompiledSpecifierType.isEntitySpecifier(type))
            return CompiledSpecifierType.REF_ENTITY;
        else if (CompiledSpecifierType.isPlayerSpecifier(type))
            return CompiledSpecifierType.REF_PLAYER;

        String refName = type.getClassName();
        if (refName == null)
            throw new IllegalArgumentException("Unable to resolve type reference for type '" + type + "'");
        else if (refName.endsWith("[]"))
            refName = refName.substring(0, refName.length() - 2);

        if (Descriptors.isPrimitive(refName))
            return getPrimitiveReference(Descriptors.primitiveToDescriptor(refName));

        ClassNode refTo = this.core.getClassLoader().getClassByName(refName);
        if (refTo == null)
            throw new IllegalArgumentException("Unable to resolve type reference for type '" + refName + " (missing class path?)");

        return this.getRefByRawType(type, refTo);
    }

    private TypeReference getRefByRawType(Type type, ClassNode refTo)
    {
        // Enumなら特別な処理を行う
        if ((refTo.access & Opcodes.ACC_ENUM) != 0)
            return this.getEnumReference(type, refTo);


        // それ以外のクラスは通常処理
        TypeReference reference = this.resolveCompiledType(type);
        if (reference == null)
            throw new IllegalArgumentException("Unable to resolve type reference for type '" + refTo.name + "'");

        return reference;
    }

    private TypeReference getEnumReference(Type type, @NotNull ClassNode classNode)
    {
        // 既に Enum としてコンパイルされているかどうかを確認
        TypeReference compiledRef = this.resolveCompiledType(type);
        if (compiledRef != null)
            return compiledRef;

        // コンパイルされていない場合は新たにコンパイル

        List<String> enumValues = classNode.fields.stream()
                .map(f -> f.name)
                .filter(f -> !(f.equals("$VALUES") || f.equals("lookup") || f.equals("id")))
                .toList();

        String className = type.getClassName();
        String classNameSimple = className.substring(className.lastIndexOf('/') + 1);
        String id = classNameToId(className);

        CompiledStringType cType = new CompiledStringType(
                new CompiledType(
                        id,
                        classNameSimple,
                        className
                ),
                enumValues
        );

        TypeReference reference = new TypeReference(id, cType);
        this.compiledItemReferences.put(id, reference);

        return reference;
    }

    public static String classNameToId(String className)
    {
        className = className.replace('.', '/');

        // java.lang.String => j.l.String のように変換する。
        String[] parts = className.split("/");
        StringBuilder id = new StringBuilder();
        for (int i = 0; i < parts.length; i++)
        {
            String part = parts[i];
            if (i == 0)
                id.append(part.charAt(0));
            else if (i == parts.length - 1)
                id.append('.').append(part);
            else
                id.append('.').append(part.charAt(0));
        }

        return id.toString();
    }

    private static TypeReference getPrimitiveReference(char desc)
    {
        return switch (desc)
        {
            case 'Z' -> PRIMITIVE_BOOLEAN;
            case 'B' -> PRIMITIVE_BYTE;
            case 'C' -> PRIMITIVE_CHAR;
            case 'S' -> PRIMITIVE_SHORT;
            case 'I' -> PRIMITIVE_INT;
            case 'J' -> PRIMITIVE_LONG;
            case 'F' -> PRIMITIVE_FLOAT;
            case 'D' -> PRIMITIVE_DOUBLE;
            default -> throw new IllegalArgumentException("Unknown primitive type descriptor: " + desc);
        };
    }

    private static class PrimitiveType extends CompiledType implements IPrimitiveType
    {
        private PrimitiveType(String primitiveName, Class<?> clazz)
        {
            super(primitiveName, primitiveName, clazz.getName());
        }

        public static TypeReference ofRef(String primitiveName, Class<?> clazz)
        {
            return new TypeReference(primitiveName, new PrimitiveType(primitiveName, clazz));
        }
    }

}
