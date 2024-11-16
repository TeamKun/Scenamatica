package org.kunlab.scenamatica.action.input;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenario.BrokenReferenceException;
import org.kunlab.scenamatica.exceptions.scenariofile.InvalidScenarioFileException;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.action.input.InputReference;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.input.Traverser;
import org.kunlab.scenamatica.interfaces.scenario.SessionStorage;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.structures.ObjectNode;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Data
public class InputReferenceImpl<T> implements InputReference<T>
{
    public static final String REFERENCE_PATTERN_PF = "${%s}";
    public static final Pattern REFERENCE_PATTERN = Pattern.compile("\\$\\{([\\w_\\-.]+)}");

    @NotNull
    private final InputToken<T> token;
    @Nullable
    private final Object referencing;
    @Nullable
    private final Object rawValue;
    private final String[] referenceParts;

    @Setter(AccessLevel.NONE)
    private T value;
    @Setter(AccessLevel.NONE)
    private boolean isResolved;

    public InputReferenceImpl(@NotNull InputToken<T> token, @Nullable Object referencing, T value, @Nullable Object rawValue, boolean isResolved) throws YamlParsingException
    {
        this.token = token;
        this.referencing = referencing;
        this.referenceParts = referencing == null ? null: selectReferences(referencing);
        this.value = value;
        this.rawValue = rawValue;
        this.isResolved = isResolved;
    }

    private static String[] selectReferences(Object referencing) throws YamlParsingException
    {
        // awdawd${hoge} -> [hoge]
        // awdawd${hoge}${fuga} -> [hoge, fuga]
        // awdawd${hoge}${fuga}${piyo} -> [hoge, fuga, piyo]

        // { awd: "${hoge}", fuga: "${fuga}" } -> [hoge, fuga]
        // [ "${hoge}", "${fuga}" ] -> [hoge, fuga]

        if (referencing instanceof StructuredYamlNode)
            return selectReferences((StructuredYamlNode) referencing);
        if (referencing instanceof String)
            return selectReferences((String) referencing).toArray(new String[0]);
        else if (referencing instanceof Iterable)
        {
            Iterable<?> iterable = (Iterable<?>) referencing;
            Set<String> references = new HashSet<>();
            for (Object obj : iterable)
            {
                if (obj != null)
                    references.addAll(selectReferences(obj.toString()));
            }
            return references.toArray(new String[0]);
        }
        else if (referencing instanceof Map)
        {
            Set<String> references = new HashSet<>();
            for (Object obj : ((Map<?, ?>) referencing).values())
            {
                if (obj != null)
                    references.addAll(selectReferences(obj.toString()));
            }

            return references.toArray(new String[0]);
        }
        else
            throw new IllegalArgumentException("Unsupported reference type: " + referencing.getClass().getName());

    }

    private static String[] selectReferences(StructuredYamlNode node) throws YamlParsingException
    {
        if (node.isType(YAMLNodeType.STRING))
            return selectReferences(node.asString()).toArray(new String[0]);
        else if (node.isType(YAMLNodeType.LIST))
        {
            Set<String> references = new HashSet<>();
            for (String child : node.asList(StructuredYamlNode::asString))
                references.addAll(selectReferences(child));

            return references.toArray(new String[0]);
        }
        else if (node.isType(YAMLNodeType.MAPPING))
        {
            Set<String> references = new HashSet<>();
            for (String child : node.asMap(StructuredYamlNode::asString, StructuredYamlNode::asString).values())
                references.addAll(selectReferences(child));

            return references.toArray(new String[0]);
        }
        else
            throw new IllegalArgumentException("Unsupported reference type: " + node);
    }

    private static Collection<String> selectReferences(String referencedString)
    {
        Matcher matcher = REFERENCE_PATTERN.matcher(referencedString);
        Set<String> references = new HashSet<>();

        while (matcher.find())
            references.add(matcher.group(1));

        return references;
    }

    public static <D> InputReference<D> valued(InputToken<D> token, D value) throws YamlParsingException
    {
        return new InputReferenceImpl<>(token, null, value, value, true);
    }

    public static <D> InputReference<D> valuedCast(InputToken<D> token, StructureSerializer serializer, Object value)
            throws InvalidScenarioFileException
    {
        return new InputReferenceImpl<>(token, null, smartCast(token, serializer, value), value, true);
    }

    @SneakyThrows
    public static <D> InputReference<D> empty(InputToken<D> token)
    {
        return new InputReferenceImpl<>(token, null, null, null, false);
    }

    public static <D> InputReference<D> references(InputToken<D> token, Object referencing) throws YamlParsingException
    {
        return new InputReferenceImpl<>(token, referencing, null, null, false);
    }

    public static boolean containsReference(String str)
    {
        return REFERENCE_PATTERN.matcher(str).find();
    }

    public static boolean containsReference(Collection<?> collection) throws YamlParsingException
    {
        for (Object obj : collection)
            if (containsReference(obj))
                return true;

        return false;
    }

    public static boolean containsReference(Map<?, ?> map) throws YamlParsingException
    {
        for (Object obj : map.values())
            if (containsReference(obj))
                return true;

        return false;
    }

    public static boolean containsReference(Object obj) throws YamlParsingException
    {
        if (obj == null)
            return false;
        else if (obj instanceof Collection)
            return containsReference((Collection<?>) obj);
        else if (obj instanceof Map)
            return containsReference((Map<?, ?>) obj);
        else if (obj instanceof StructuredYamlNode)
            return containsReference((StructuredYamlNode) obj);
        else
            return containsReference(obj.toString());
    }

    private static boolean containsReference(StructuredYamlNode node) throws YamlParsingException
    {
        if (node.getThisNode() instanceof ObjectNode)
            return false;
        else if (node.isType(YAMLNodeType.STRING))
            return containsReference(node.asString());
        else if (node.isType(YAMLNodeType.LIST))
            return containsReference(node.asList(StructuredYamlNode::asObject));
        else if (node.isType(YAMLNodeType.MAPPING))
            return containsReference(node.asMap(StructuredYamlNode::asObject, StructuredYamlNode::asObject));
        else
            return false;
    }

    private static Object resolveReferences(String base, String[] references, SessionStorage variables)
    {
        boolean isOnlyReference = references.length == 1 && base.equals(String.format(REFERENCE_PATTERN_PF, references[0]));

        boolean containsNull = false;
        for (String reference : references)
        {
            Object obj = variables.get(reference);
            if (isOnlyReference)
                return obj;

            containsNull |= obj == null;

            String value = obj == null ? "": obj.toString();
            base = base.replace(String.format(REFERENCE_PATTERN_PF, reference), value);
        }


        // 文字列が空・null が含まれている => すべて null
        return base.isEmpty() && containsNull ? null: base;
    }

    private static Collection<?> resolveReferences(Collection<?> base, String[] references, SessionStorage variables)
    {
        List<Object> resolved = new ArrayList<>();
        for (Object obj : base)
        {
            Object resolvedObj;
            if (obj instanceof Collection)
                resolvedObj = resolveReferences((Collection<?>) obj, references, variables);
            else if (obj instanceof Map)
                resolvedObj = resolveReferences((Map<?, ?>) obj, references, variables);
            else if (obj == null)
                resolvedObj = null;
            else
                resolvedObj = resolveReferences(obj.toString(), references, variables);

            resolved.add(resolvedObj);
        }

        return resolved;
    }

    private static Map<?, ?> resolveReferences(Map<?, ?> base, String[] references, SessionStorage variables)
    {
        Map<Object, Object> resolved = new HashMap<>();
        for (Map.Entry<?, ?> entry : base.entrySet())
        {
            Object value = entry.getValue();
            // キーはスコープ外

            Object resolvedValue;
            if (entry.getValue() instanceof Collection)
                resolvedValue = resolveReferences((Collection<?>) value, references, variables);
            else if (entry.getValue() instanceof Map)
                resolvedValue = resolveReferences((Map<?, ?>) value, references, variables);
            else if (entry.getValue() == null)
                resolvedValue = null;
            else
                resolvedValue = resolveReferences(value.toString(), references, variables);

            resolved.put(entry.getKey(), resolvedValue);
        }

        return resolved;
    }

    private static Object resolveReferences(Object base, String[] references, SessionStorage variables)
    {
        if (base instanceof Collection)
            return resolveReferences((Collection<?>) base, references, variables);
        else if (base instanceof Map)
            return resolveReferences((Map<?, ?>) base, references, variables);
        else if (base == null)
            return null;
        else
            return resolveReferences(base.toString(), references, variables);
    }

    private static <T extends Number> T castNumber(Number num, Class<? extends T> clazz)
    {
        if (clazz == Integer.class || clazz == int.class)
            return clazz.cast(num.intValue());
        else if (clazz == Long.class || clazz == long.class)
            return clazz.cast(num.longValue());
        else if (clazz == Double.class || clazz == double.class)
            return clazz.cast(num.doubleValue());
        else if (clazz == Float.class || clazz == float.class)
            return clazz.cast(num.floatValue());
        else
            throw new IllegalArgumentException("Unknown number type: " + clazz);
    }

    private static <T extends Number> T smartCastNumber(Object obj, Class<T> clazz) throws YamlParsingException
    {
        if (obj instanceof Number)  // 自分自身が Number ならそのままキャスト
            return castNumber((Number) obj, clazz);
        else if (obj instanceof String) // 数を表す文字列なら BigDecimal に変換してキャスト
        {
            BigDecimal decimal = new BigDecimal((String) obj);
            return castNumber(decimal, clazz);
        }
        else if (obj instanceof StructuredYamlNode)  // YAML なら Number か判定してキャスト
        {
            StructuredYamlNode node = (StructuredYamlNode) obj;
            node.ensureTypeOf(YAMLNodeType.NUMBER);

            return castNumber(node.asNumber(), clazz);
        }
        else
            throw new IllegalArgumentException("Unknown number type: " + clazz);
    }

    private static Boolean smartCastBoolean(Object obj) throws YamlParsingException
    {
        if (obj instanceof Boolean)
            return (Boolean) obj;  // Boolean#parseBoolean は false になるので使わない
        else if (obj instanceof String)  // 文字列なら Boolean に変換してキャスト
        {
            boolean isTrue = Boolean.parseBoolean((String) obj);  // これは "false" かどうか, 不正文字列かを判定しない。
            if (isTrue || "false".equalsIgnoreCase((String) obj))
                return isTrue;
            else
                throw new IllegalArgumentException("Unable top parse boolean from string: " + obj);
        }
        else if (obj instanceof StructuredYamlNode)  // YAML なら Boolean か判定してキャスト
        {
            StructuredYamlNode node = (StructuredYamlNode) obj;
            node.ensureTypeOf(YAMLNodeType.BOOLEAN);

            return node.asBoolean();
        }
        else
            throw new IllegalArgumentException("Unknown boolean type: " + obj);
    }

    private static String smartCastString(Object obj) throws YamlParsingException
    {
        if (obj instanceof String)
            return (String) obj;
        else if (obj instanceof StructuredYamlNode)
            return ((StructuredYamlNode) obj).asString();
        else
            return String.valueOf(obj);
    }

    private static <T> T smartCast(Object obj, Class<? extends T> clazz) throws YamlParsingException
    {
        if (obj == null)
            return null;
        else if (clazz.isInstance(obj))
            return clazz.cast(obj);

        if ((obj instanceof ObjectNode && ((ObjectNode) obj).isType(clazz)))
            return clazz.cast(((ObjectNode) obj).getObject());
        else if (obj instanceof StructuredYamlNode && ((StructuredYamlNode) obj).getThisNode() instanceof ObjectNode)
        {
            ObjectNode node = (ObjectNode) ((StructuredYamlNode) obj).getThisNode();
            if (node.isType(clazz))
                return clazz.cast(node.getObject());
        }

        if (Number.class.isAssignableFrom(clazz))
            // noinspection unchecked
            return clazz.cast(smartCastNumber(obj, (Class<? extends Number>) clazz));
        else if (clazz == Boolean.class || clazz == boolean.class)
            return clazz.cast(smartCastBoolean(obj));
        else if (List.class.isAssignableFrom(clazz))
        {
            if (obj instanceof List)
                return clazz.cast(obj);
            else if (obj instanceof StructuredYamlNode)
            {
                StructuredYamlNode node = (StructuredYamlNode) obj;
                node.ensureTypeOf(YAMLNodeType.LIST);

                return clazz.cast(node.asList(StructuredYamlNode::asObject));
            }
            else
                throw new IllegalArgumentException("Unknown list type: " + obj);
        }
        else if (clazz == Map.class)
        {
            if (obj instanceof Map)
                return clazz.cast(obj);
            else if (obj instanceof StructuredYamlNode)
            {
                StructuredYamlNode node = (StructuredYamlNode) obj;
                node.ensureTypeOf(YAMLNodeType.MAPPING);

                return clazz.cast(node.asMap(StructuredYamlNode::asObject, StructuredYamlNode::asObject));
            }
            else
                throw new IllegalArgumentException("Unknown map type: " + obj);
        }
        else if (clazz == String.class)
            return clazz.cast(smartCastString(obj));
        else
            throw new IllegalArgumentException("Unknown type: " + obj + ", expected: " + clazz);
    }

    private static boolean canSmartCast(Class<?> clazz, Object obj)
    {
        return obj == null
                || clazz.isInstance(obj)
                || clazz == String.class
                || (Number.class.isAssignableFrom(clazz) && obj instanceof Number)
                || (List.class.isAssignableFrom(clazz) && obj instanceof List)
                || (Map.class.isAssignableFrom(clazz) && obj instanceof Map)
                || (Boolean.class.isAssignableFrom(clazz) && obj instanceof Boolean)
                || (obj instanceof StructuredYamlNode);
    }

    private static <U> U smartCast(@NotNull InputToken<U> token, @NotNull StructureSerializer serializer, @Nullable Object resolved)
            throws InvalidScenarioFileException
    {
        if (resolved == null)
            return null;

        List<Traverser<?, U>> traversers = token.getTraversers();
        if (traversers.isEmpty())
            return smartCast(resolved, token.getClazz());
        else
        {
            for (Traverser<?, U> traverser : traversers)
            {
                Class<?> possibleType = traverser.getInputClazz();
                if (canSmartCast(possibleType, resolved))
                    return token.getClazz().cast(traverser.tryTraverse(serializer, smartCast(resolved, possibleType)));
                else if (possibleType.isInstance(resolved))
                    return token.getClazz().cast(traverser.tryTraverse(serializer, resolved));
            }

            if (token.getClazz().isInstance(resolved))
                return token.getClazz().cast(resolved);
            else
                throw new IllegalArgumentException("Incompatible type detected for " + token.getName() + ": " + resolved.getClass().getName());
        }
    }

    @Override
    public boolean isEquals(String reference)
    {
        return Objects.equals(this.referencing, reference);
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object)
            return true;
        if (!(object instanceof InputReferenceImpl))
            return false;
        InputReferenceImpl<?> that = (InputReferenceImpl<?>) object;
        return Objects.equals(this.getReferencing(), that.getReferencing())
                && Objects.equals(this.getValue(), that.getValue());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.getReferencing(), this.getValue());
    }

    @Override
    public void resolve(@Nullable T value)
    {
        this.value = value;
        this.isResolved = true;
    }

    @Override
    public void resolve(@NotNull StructureSerializer serializer, @NotNull SessionStorage variables)
            throws InvalidScenarioFileException
    {
        if (this.rawValue != null)
        {
            this.resolve(this.smartCast(serializer, this.rawValue));
            return;
        }
        if (this.referenceParts == null)
            throw new BrokenReferenceException(null, "This reference doesn't contain any references: " + this.referencing);
        assert this.referencing != null;

        Object resolved = resolveReferences(this.referencing, this.referenceParts, variables);

        if (containsReference(resolved))
            throw new BrokenReferenceException(null, "Failed to resolve reference: " + this.referencing + " -> " + resolved);

        this.resolve(this.smartCast(serializer, resolved));
    }

    @Override
    public void release()
    {
        if (this.referencing == null && this.value == this.rawValue)
            return; // 最適化
        this.value = null;
        this.isResolved = false;
    }

    @Override
    public boolean isEmpty()
    {
        return this.value == null && this.referencing == null && this.rawValue == null;
    }

    private T smartCast(@NotNull StructureSerializer serializer, @Nullable Object resolved)
            throws InvalidScenarioFileException
    {
        return smartCast(this.token, serializer, resolved);
    }
}
