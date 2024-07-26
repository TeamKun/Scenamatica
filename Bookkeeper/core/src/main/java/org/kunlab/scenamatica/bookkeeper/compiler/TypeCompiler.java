package org.kunlab.scenamatica.bookkeeper.compiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.BookkeeperCore;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledSpecifierType;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledStringType;
import org.kunlab.scenamatica.bookkeeper.compiler.models.CompiledType;
import org.kunlab.scenamatica.bookkeeper.compiler.models.GenericAdmonition;
import org.kunlab.scenamatica.bookkeeper.compiler.models.IPrimitiveType;
import org.kunlab.scenamatica.bookkeeper.compiler.models.refs.TypeReference;
import org.kunlab.scenamatica.bookkeeper.definitions.TypeDefinition;
import org.kunlab.scenamatica.bookkeeper.definitions.TypePropertyDefinition;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TypeCompiler extends AbstractCompiler<TypeDefinition, CompiledType, TypeReference>
{
    private static final TypeReference PRIMITIVE_BOOLEAN = PrimitiveType.ofRef("boolean", boolean.class);
    private static final TypeReference PRIMITIVE_BYTE = PrimitiveType.ofRef("byte", byte.class);
    private static final TypeReference PRIMITIVE_CHAR = PrimitiveType.ofRef("char", char.class);
    private static final TypeReference PRIMITIVE_SHORT = PrimitiveType.ofRef("short", short.class);
    private static final TypeReference PRIMITIVE_INT = PrimitiveType.ofRef("integer", int.class);
    private static final TypeReference PRIMITIVE_LONG = PrimitiveType.ofRef("long", long.class);
    private static final TypeReference PRIMITIVE_FLOAT = PrimitiveType.ofRef("float", float.class);
    private static final TypeReference PRIMITIVE_DOUBLE = PrimitiveType.ofRef("double", double.class);
    private static final TypeReference PRIMITIVE_OBJECT = PrimitiveType.ofRef("object", Object.class);

    private static final TypeReference CO_PRIMITIVE_STRING = PrimitiveType.ofRef("string", String.class);
    private static final TypeReference CO_PRIMITIVE_MAP = PrimitiveType.ofRef("object", Map.class);

    private static final MessageDigest SHA1_DIGEST;

    private final BookkeeperCore core;
    private final CategoryManager categoryManager;
    private CategoryManager.CategoryEntry enumCategory;

    static
    {
        try
        {
            SHA1_DIGEST = MessageDigest.getInstance("SHA-1");
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalArgumentException(e);
        }
    }

    public TypeCompiler(BookkeeperCore core)
    {
        super(core, "types");
        this.core = core;
        this.categoryManager = core.getCategoryManager();
    }

    public TypeReference lookup(Type type)
    {
        TypeReference reference;
        if ((reference = this.resolveCompiledType(type)) != null)
            return reference;
        else if ((reference = this.resolveOtherType(type)) != null)
            return reference;

        throw new IllegalArgumentException("Unable to resolve type reference for type '" + type.getClassName() + "'");
    }

    @Override
    protected String toId(CompiledType compiledItem)
    {
        return compiledItem.getId();
    }

    @Override
    public void prepare()
    {
        this.enumCategory = this.categoryManager.registerCategoryManually("enums", "列挙型", "自動生成された列挙型");
    }

    @Override
    protected TypeReference doCompile(TypeDefinition definition)
    {
        String id = toId(definition);
        if (this.resolve(id) != null)
            throw new IllegalArgumentException("Type with ID '" + id + "' already exists, consider changing the name of the type.");

        Type parentType = definition.getExtending();
        CompiledType parentCompiled = null;
        if (parentType != null)
        {
            TypeReference parentRef = this.lookup(parentType);
            parentCompiled = parentRef.getResolved();
        }

        CategoryManager.CategoryEntry category = this.categoryManager.recogniseCategory(definition.getAnnotatedClass());
        if (category == null && parentCompiled != null)
            category = parentCompiled.getCategory();

        List<GenericAdmonition> admonitions = new ArrayList<>(Arrays.asList(definition.getAdmonitions()));
        if (parentCompiled != null && parentCompiled.getAdmonitions() != null)
            admonitions.addAll(Arrays.asList(parentCompiled.getAdmonitions()));

        return new TypeReference(
                id,
                new CompiledType(
                        id,
                        definition.getName(),
                        definition.getDescription(),
                        category,
                        definition.getClazz().name,
                        definition.getMappingOf() != null ? definition.getMappingOf().getClassName().replace('.', '/'): null,
                        compileProperties(definition.getProperties(), parentCompiled),
                        admonitions.toArray(new GenericAdmonition[0])
                )
        );
    }

    @Override
    protected String toId(TypeDefinition definition)
    {
        return createClassNameStringReference(definition.getClazz().name);
    }

    private Map<String, CompiledType.Property> compileProperties(TypePropertyDefinition[] properties, @Nullable CompiledType parent)
    {
        Map<String, CompiledType.Property> compiledProperties = new HashMap<>();
        if (parent != null)
        {
            Map<String, CompiledType.Property> parentProperties = parent.getProperties();
            if (parentProperties != null)
                compiledProperties.putAll(parentProperties);
        }

        if (properties == null)
            return compiledProperties;

        for (TypePropertyDefinition property : properties)
        {
            compiledProperties.put(
                    property.getName(),
                    new CompiledType.Property(
                            property.getName(),
                            resolveOtherType(property.getType()),
                            property.getDescription(),
                            property.isRequired(),
                            property.getType().getDescriptor().startsWith("["),
                            property.getPattern(),
                            property.getMin(),
                            property.getMax(),
                            property.getDefaultValue(),
                            property.getAdmonitions()
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
            if (Descriptors.isClassNameEqual(asmType.getClassName(), compiledType.getClassName())
                    || Descriptors.isClassNameEqual(asmType.getClassName(), compiledType.getMappingOf()))
                return reference;
        }

        return null;
    }

    @Override
    public Class<TypeDefinition> getDefinitionType()
    {
        return TypeDefinition.class;
    }

    @Override
    protected String getFileLocation(TypeReference reference)
    {
        String superLocation = super.getFileLocation(reference);
        if (reference.getResolved() instanceof CompiledStringType)
        {
            CompiledStringType stringType = (CompiledStringType) reference.getResolved();
            switch (stringType.type())
            {
                case CompiledStringType.FORMAT_TYPE:
                    return "formats/" + superLocation;
                case CompiledStringType.PATTERN_TYPE:
                    return "patterns/" + superLocation;
                case CompiledStringType.ENUMS_TYPE:
                    return "enums/" + superLocation;
            }
        }

        String sup = super.getFileLocation(reference);
        CategoryManager.CategoryEntry categoryReference = reference.getResolved().getCategory();
        if (categoryReference == null)
            return sup;

        return categoryReference.getChildrenPath().resolve(sup).toString();
    }

    private TypeReference resolveOtherType(Type type)
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
        else if (desc.equals(CompiledStringType.DESC_NAMESPACED_KEY))
            return CompiledStringType.REF_NAMESPACED_KEY;
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
                .filter(f -> (f.access & Opcodes.ACC_ENUM) != 0)
                .map(f -> f.name)
                .collect(Collectors.toList());

        String className = type.getClassName();
        String classNameSimple = Descriptors.convertSimpleClassName(className);
        String id = createClassNameStringReference(className);

        CategoryManager.CategoryEntry category = this.categoryManager.recogniseCategory(classNode);
        if (category == null)
            category = this.enumCategory;

        CompiledStringType cType = new CompiledStringType(
                new CompiledType(
                        id,
                        classNameSimple,
                        null,
                        category,
                        className
                ),
                enumValues
        );

        TypeReference reference = new TypeReference(id, cType);
        this.compiledItemReferences.put(id, reference);

        return reference;
    }

    private static String getSHA1Hash(String input, int fLen)
    {
        byte[] hash = SHA1_DIGEST.digest(input.getBytes());
        StringBuilder sb = new StringBuilder(fLen);
        for (int i = 0; i < fLen; i++)
        {
            int b = hash[i] & 0xFF;
            if (b < 0x10)
                sb.append('0');
            sb.append(Integer.toHexString(b));
        }

        return sb.toString();
    }

    public static String createClassNameStringReference(String classNameFull)
    {
        classNameFull = classNameFull.replace('.', '/');  // 表記揺れがたまにある

        String packageName;
        String className;

        int lastSlash = classNameFull.lastIndexOf('/');
        if (lastSlash == -1)
        {
            packageName = "";
            className = classNameFull;
        }
        else
        {
            packageName = classNameFull.substring(0, lastSlash);
            className = classNameFull.substring(lastSlash + 1);
        }

        return className /*+ "-" + getSHA1Hash(packageName, 8)*/;
    }

    private static TypeReference getPrimitiveReference(char desc)
    {
        switch (desc)
        {
            case 'Z':
                return PRIMITIVE_BOOLEAN;
            case 'B':
                return PRIMITIVE_BYTE;
            case 'C':
                return PRIMITIVE_CHAR;
            case 'S':
                return PRIMITIVE_SHORT;
            case 'I':
                return PRIMITIVE_INT;
            case 'J':
                return PRIMITIVE_LONG;
            case 'F':
                return PRIMITIVE_FLOAT;
            case 'D':
                return PRIMITIVE_DOUBLE;
            case 'L':
                return PRIMITIVE_OBJECT;
            default:
                throw new IllegalArgumentException("Unknown primitive type descriptor: " + desc);
        }
    }

    public static class PrimitiveType extends CompiledType implements IPrimitiveType
    {
        private PrimitiveType(String primitiveName, Class<?> clazz)
        {
            super(primitiveName, primitiveName, null, null, clazz.getName());
        }

        @Override
        public Map<String, Object> serialize(@NotNull SerializingContext ctxt)
        {
            Map<String, Object> map = super.serialize(ctxt);
            if (ctxt.isJSONSchema())
                map.putAll(toJSONPrimitive(this.getId()));
            return map;
        }

        public static TypeReference ofRef(String primitiveName, Class<?> clazz)
        {
            return new TypeReference(primitiveName, new PrimitiveType(primitiveName, clazz));
        }

        private static Map<String, Object> toJSONPrimitive(String javaPrimitive)
        {
            Map<String, Object> properties = new HashMap<>();
            switch (javaPrimitive)
            {
                case "boolean":
                    properties.put("type", "boolean");
                    break;
                case "byte":
                    properties.put("type", "number");
                    properties.put("format", "int8");
                    break;
                case "char":
                    properties.put("type", "string");
                    properties.put("maxLength", 1);
                    break;
                case "short":
                    properties.put("type", "number");
                    properties.put("format", "int16");
                    break;
                case "integer":
                    properties.put("type", "number");
                    properties.put("format", "int32");
                    break;
                case "long":
                    properties.put("type", "integer");
                    properties.put("format", "int64");
                    break;
                case "float":
                    properties.put("type", "number");
                    properties.put("format", "float");
                    break;
                case "double":
                    properties.put("type", "number");
                    properties.put("format", "double");
                    break;
                case "object":
                    properties.put("type", "object");
                    break;
                case "string":
                    properties.put("type", "string");
                    break;
            }

            return properties;
        }
    }

}
