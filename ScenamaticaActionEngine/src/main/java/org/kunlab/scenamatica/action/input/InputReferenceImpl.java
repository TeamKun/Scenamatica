package org.kunlab.scenamatica.action.input;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.interfaces.action.input.InputReference;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.input.Traverser;
import org.kunlab.scenamatica.interfaces.scenario.SessionVariableHolder;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

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
    private final String[] referenceParts;

    @Setter(AccessLevel.NONE)
    private T value;
    @Setter(AccessLevel.NONE)
    private boolean isResolved;

    public InputReferenceImpl(@NotNull InputToken<T> token, @Nullable Object referencing, T value, boolean isResolved)
    {
        this.token = token;
        this.referencing = referencing;
        this.referenceParts = referencing == null ? null: selectReferences(referencing);
        this.value = value;
        this.isResolved = isResolved;
    }

    private static String[] selectReferences(Object referencing)
    {
        // awdawd${hoge} -> [hoge]
        // awdawd${hoge}${fuga} -> [hoge, fuga]
        // awdawd${hoge}${fuga}${piyo} -> [hoge, fuga, piyo]

        // { awd: "${hoge}", fuga: "${fuga}" } -> [hoge, fuga]
        // [ "${hoge}", "${fuga}" ] -> [hoge, fuga]

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

    private static Collection<String> selectReferences(String referencedString)
    {
        Matcher matcher = REFERENCE_PATTERN.matcher(referencedString);
        Set<String> references = new HashSet<>();

        while (matcher.find())
            references.add(matcher.group(1));

        return references;
    }

    public static <D> InputReference<D> valued(InputToken<D> token, D value)
    {
        return new InputReferenceImpl<>(token, null, value, true);
    }

    public static <D> InputReference<D> valuedCast(InputToken<D> token, StructureSerializer serializer, Object value)
    {
        return new InputReferenceImpl<>(token, null, smartCast(token, serializer, value), true);
    }

    public static <D> InputReference<D> empty(InputToken<D> token)
    {
        return new InputReferenceImpl<>(token, null, null, false);
    }

    public static <D> InputReference<D> references(InputToken<D> token, Object referencing)
    {
        return new InputReferenceImpl<>(token, referencing, null, false);
    }

    public static boolean containsReference(String str)
    {
        return REFERENCE_PATTERN.matcher(str).find();
    }

    public static boolean containsReference(Collection<?> collection)
    {
        for (Object obj : collection)
            if (containsReference(obj))
                return true;

        return false;
    }

    public static boolean containsReference(Map<?, ?> map)
    {
        for (Object obj : map.values())
            if (containsReference(obj))
                return true;

        return false;
    }

    public static boolean containsReference(Object obj)
    {
        if (obj == null)
            return false;
        else if (obj instanceof Collection)
            return containsReference((Collection<?>) obj);
        else if (obj instanceof Map)
            return containsReference((Map<?, ?>) obj);
        else
            return containsReference(obj.toString());
    }

    private static String resolveReferences(String base, String[] references, SessionVariableHolder variables)
    {
        boolean containsNull = false;
        for (String reference : references)
        {
            Object obj = variables.get(reference);
            containsNull |= obj == null;

            String value = obj == null ? "": obj.toString();
            base = base.replace(String.format(REFERENCE_PATTERN_PF, reference), value);
        }


        // 文字列が空・null が含まれている => すべて null
        return base.isEmpty() && containsNull ? null: base;
    }

    private static Collection<?> resolveReferences(Collection<?> base, String[] references, SessionVariableHolder variables)
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

    private static Map<?, ?> resolveReferences(Map<?, ?> base, String[] references, SessionVariableHolder variables)
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

    private static Object resolveReferences(Object base, String[] references, SessionVariableHolder variables)
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

    private static <T extends Number> T smartCastNumber(Number num, Class<? extends T> clazz)
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

    private static <T> T smartCast(Object obj, Class<? extends T> clazz)
    {
        if (obj == null)
            return null;
        else if (clazz.isInstance(obj))
            return clazz.cast(obj);

        if (Number.class.isAssignableFrom(clazz))
        {
            if (obj instanceof Number)
            {
                // noinspection unchecked
                return clazz.cast(smartCastNumber((Number) obj, (Class<? extends Number>) clazz));
            }
            else if (obj instanceof String)
            {
                BigDecimal decimal = new BigDecimal((String) obj);
                // noinspection unchecked
                return clazz.cast(smartCastNumber(decimal, (Class<? extends Number>) clazz));
            }
            else
                throw new IllegalArgumentException("Unknown number type: " + clazz);
        }
        else if (clazz == Boolean.class || clazz == boolean.class)
        {
            if (obj instanceof Boolean)
                return clazz.cast(obj);  // Boolean#parseBoolean は false になるので使わない
            else if (obj instanceof String && ((String) obj).equalsIgnoreCase("true"))
                return clazz.cast(true);
            else if (obj instanceof String && ((String) obj).equalsIgnoreCase("false"))
                return clazz.cast(false);
            else
                throw new IllegalArgumentException("Unknown boolean type: " + obj);
        }
        else if (clazz == List.class)
        {
            if (obj instanceof List)
                return clazz.cast(obj);
            else
                throw new IllegalArgumentException("Unknown list type: " + obj);
        }
        else if (clazz == Map.class)
        {
            if (obj instanceof Map)
                return clazz.cast(obj);
            else
                throw new IllegalArgumentException("Unknown map type: " + obj);
        }
        else if (clazz == String.class)
            return clazz.cast(obj.toString());
        else
            throw new IllegalArgumentException("Unknown type: " + obj);
    }

    private static boolean canSmartCast(Class<?> clazz, Object obj)
    {
        return obj == null
                || clazz.isInstance(obj)
                || clazz == String.class
                || (Number.class.isAssignableFrom(clazz) && obj instanceof Number)
                || (clazz == Boolean.class && obj instanceof Boolean)
                || (clazz == List.class && obj instanceof List)
                || (clazz == Map.class && obj instanceof Map);
    }

    private static <U> U smartCast(@NotNull InputToken<U> token, @NotNull StructureSerializer serializer, @Nullable Object resolved)
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

            throw new IllegalArgumentException("Unknown traverser type");
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
    public void resolve(@NotNull StructureSerializer serializer, @NotNull SessionVariableHolder variables)
    {
        if (this.referenceParts == null)
            throw new IllegalStateException("This reference doesn't contain any references: " + this.referencing);
        assert this.referencing != null;

        Object resolved = resolveReferences(this.referencing, this.referenceParts, variables);

        if (containsReference(resolved))
            throw new IllegalStateException("Failed to resolve reference: " + this.referencing + " -> " + resolved);

        this.resolve(this.smartCast(serializer, resolved));
    }

    @Override
    public boolean isEmpty()
    {
        return this.value == null && this.referencing == null;
    }

    private T smartCast(@NotNull StructureSerializer serializer, @Nullable Object resolved)
    {
        return smartCast(this.token, serializer, resolved);
    }
}
