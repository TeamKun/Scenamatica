package org.kunlab.scenamatica.bookkeeper;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.kunlab.scenamatica.bookkeeper.utils.Descriptors;
import org.objectweb.asm.tree.AnnotationNode;

import java.lang.reflect.Array;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AnnotationValues
{
    private final Map<String, Object> values;

    public Object get(String key)
    {
        return this.values.get(key);
    }

    public <T> T get(String key, Class<T> type)
    {
        try
        {
            return type.cast(this.values.get(key));
        }
        catch (ClassCastException e)
        {
            throw new IllegalArgumentException("Unexpected type of the key " + key + ". Expected: " + type.getName()
                    + ", (got " + this.values.get(key).getClass().getName() + ")");
        }
    }

    public String getAsString(String keyName)
    {
        return get(keyName, String.class);
    }

    public <T> T[] getAsArray(String key, Class<? extends T> arrayType)
    {
        return this.getAsArray(key, arrayType, arrayType::cast);
    }

    public <T extends Enum<T>> T[] getAsEnumArray(String key, Class<T> enumType)
    {
        return this.getAsArray(key, enumType, (obj) -> {
            try
            {
                String[] name = (String[]) obj;
                if (name.length != 2)
                    throw new IllegalArgumentException("Broken reference array for enum type " + enumType.getName());

                String typeName = name[0];
                String value = name[1];

                if (Descriptors.getDescriptor(enumType).equals(typeName))
                    return Enum.valueOf(enumType, value);
                else
                    throw new IllegalArgumentException("Broken enum reference for " + enumType.getName() + ". Expected: " + Descriptors.getDescriptor(enumType) + ", (got " + typeName + ")");
            }
            catch (IllegalArgumentException e)
            {
                throw new IllegalArgumentException("The value of the key " + key + " is not a valid enum constant of " + enumType.getName(), e);
            }
        });
    }

    @SuppressWarnings("unchecked")
    public <T> T[] getAsArray(String key, Class<? extends T> clazz, Function<Object, ? extends T> mapper)
    {
        Object value = get(key);
        if (value == null)
            return null;

        if (value instanceof List<?> list)
        {
            T[] array = (T[]) Array.newInstance(clazz, list.size());
            for (int i = 0; i < list.size(); i++)
                array[i] = mapper.apply(list.get(i));
            return array;
        }
        else if (value.getClass().isArray())
            return (T[]) value;
        else
            throw new IllegalArgumentException("The value of the key " + key + " is not a valid array or list.");
    }

    public <T extends Enum<T>> T getAsEnum(String key, Class<T> enumType)
    {
        try
        {
            String[] name = getAsArray(key, String.class);
            if (name == null)
                return null;
            else if (name.length != 2)
                throw new IllegalArgumentException("Broken reference array for enum type " + enumType.getName());

            String typeName = name[0];
            String value = name[1];

            if (Descriptors.getDescriptor(enumType).equals(typeName))
                return Enum.valueOf(enumType, value);
            else
                throw new IllegalArgumentException("Broken enum reference for " + enumType.getName() + ". Expected: " + Descriptors.getDescriptor(enumType) + ", (got " + typeName + ")");
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("The value of the key " + key + " is not a valid enum constant of " + enumType.getName(), e);
        }
    }

    public boolean containsKey(String keyOutputs)
    {
        return this.values.containsKey(keyOutputs);
    }

    public boolean getAsBoolean(String keyRequired)
    {
        return Boolean.TRUE.equals(get(keyRequired, Boolean.class));
    }

    public static AnnotationValues of(AnnotationNode node)
    {
        return new AnnotationValues(Collections.unmodifiableMap(getAnnotationValues(node)));
    }

    private static Map<String, Object> getAnnotationValues(AnnotationNode annotation)
    {
        List<Object> values = annotation.values;
        if (values.size() % 2 != 0)
            throw new IllegalArgumentException("The size of the values must be even.");

        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < values.size(); i += 2)
        {
            Object keyRaw = values.get(i);
            Object value = values.get(i + 1);
            if (!(keyRaw instanceof String key))
                throw new IllegalArgumentException("The key must be a string.");

            map.put(key, value);
        }

        return map;
    }
}
