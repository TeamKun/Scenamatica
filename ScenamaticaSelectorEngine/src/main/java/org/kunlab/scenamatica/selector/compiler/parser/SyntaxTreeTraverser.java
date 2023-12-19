package org.kunlab.scenamatica.selector.compiler.parser;

import net.kunmc.lab.peyangpaperutils.lib.utils.Pair;
import org.kunlab.scenamatica.selector.SelectorType;
import org.kunlab.scenamatica.selector.compiler.SelectorSyntaxErrorException;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyntaxTreeTraverser
{
    public static final SelectorType DEFAULT_TYPE = SelectorType.ENTITY_ALL;

    public static PropertiedSelector traverse(SyntaxTree tree)
    {
        if (tree.getType() != SyntaxType.SELECTOR)
            throw SelectorSyntaxErrorException.unexpectedDeclare(tree.toString(), "non selector", 0);

        SyntaxTree typeTree = tree.getChild(SyntaxType.TYPE);
        SyntaxTree[] propertyTrees = tree.getChildren(SyntaxType.PROPERTY);

        SelectorType type = typeTree == null ? DEFAULT_TYPE: SelectorType.of(typeTree.getValue());
        Map<String, Object> properties = traverseProperties(propertyTrees);

        return new PropertiedSelector(type, properties);
    }

    private static Map<String, Object> traverseProperties(SyntaxTree[] tree)
    {
        if (tree == null)
            return null;

        Map<String, Object> properties = new HashMap<>();
        for (SyntaxTree propertyTree : tree)
        {
            Pair<String, Object> property = processOneProperty(propertyTree);

            if (properties.containsKey(property.getLeft()))
                properties.put(property.getLeft(), tryMerge(properties.get(property.getLeft()), property.getRight()));
            else
                properties.put(property.getLeft(), property.getRight());
        }

        return properties;
    }

    private static Pair<String, Object> processOneProperty(SyntaxTree tree)
    {
        if (tree.getType() != SyntaxType.PROPERTY)
            throw SelectorSyntaxErrorException.unexpectedDeclare(tree.toString(), "non property", 0);

        SyntaxTree keyTree = tree.getChild(SyntaxType.KEY);

        String key = keyTree.getValue();
        Object value = traverseValue(tree);

        return new Pair<>(key, value);
    }

    private static Object traverseValue(SyntaxTree tree)
    {
        switch (tree.getType())
        {
            case VALUE:
                return normalizeValue(tree.getValue());
            case NEGATE:
                return new NegativeValue(traverseValue(tree.getChildren()[0]));
            case COLLECTION:
                return traverseCollection(tree);
            case PROPERTY:
                SyntaxTree value = tree.getChild(SyntaxType.VALUE);
                SyntaxTree collection = tree.getChild(SyntaxType.COLLECTION);
                if (value != null)
                    return traverseValue(value);
                else if (collection != null)
                    return traverseCollection(collection);
                else
                    throw SelectorSyntaxErrorException.unexpectedDeclare(tree.toString(), "non value", 0);
            default:
                throw SelectorSyntaxErrorException.unexpectedDeclare(tree.toString(), "non value", 0);
        }
    }

    private static Object traverseCollection(SyntaxTree tree)
    {
        SyntaxTree[] property = tree.getChildren(SyntaxType.PROPERTY);
        SyntaxTree[] value = tree.getChildren(SyntaxType.VALUE);
        SyntaxTree[] collection = tree.getChildren(SyntaxType.COLLECTION); // Nested-collection value

        if (property.length != 0)
        {
            Map<String, Object> map = new HashMap<>();
            for (SyntaxTree child : property)
            {
                Pair<String, Object> pair = processOneProperty(child);
                map.put(pair.getLeft(), pair.getRight());
            }
            return map;
        }

        List<Object> list = new ArrayList<>();
        for (SyntaxTree child : value)
            list.add(traverseValue(child));
        for (SyntaxTree child : collection)
            list.add(traverseCollection(child));

        return list;
    }

    private static Object normalizeValue(String value)
    {
        if (value == null || value.equalsIgnoreCase("null"))
            return null;
        else if (value.startsWith("\"") && value.endsWith("\""))
            return value.substring(1, value.length() - 1);
        else if (value.equalsIgnoreCase("true")
                || value.equalsIgnoreCase("on")
                || value.equalsIgnoreCase("yes"))
            return true;
        else if (value.equalsIgnoreCase("false")
                || value.equalsIgnoreCase("off")
                || value.equalsIgnoreCase("no"))
            return false;

        try
        {
            return NumberFormat.getInstance().parse(value);
        }
        catch (Exception e)
        {
            return value;
        }
    }

    private static Map<String, Object> combine(Map<String, Object> map1, Map<String, Object> map2)
    {
        if (map1 == null)
            return map2;
        else if (map2 == null)
            return map1;

        Map<String, Object> combined = new HashMap<>();
        deepMerge(map1, map2, combined);
        deepMerge(map2, map1, combined);

        return combined;
    }

    private static void deepMerge(Map<String, Object> map1, Map<String, Object> map2, Map<? super String, Object> combined)
    {
        for (Map.Entry<String, Object> entry : map1.entrySet())
        {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (!map2.containsKey(key))
                combined.put(key, value);

            Object value2 = map2.get(key);

            combined.put(key, tryMerge(value, value2));
        }
    }

    private static boolean canAppend(Object obj1, Object obj2)
    {
        return obj1 instanceof List || obj2 instanceof List;
    }

    private static Object tryMerge(Object obj1, Object obj2)
    {
        if (obj1 == null)
            return obj2;
        else if (obj2 == null)
            return obj1;
        else if (canAppend(obj1, obj2))
            return appendList(obj1, obj2);
        else if (!isSameType(obj1, obj2))
            return obj2;  // obj2 を優先的に採用

        if (obj1 instanceof List)
        {
            List<?> list1 = (List<?>) obj1;
            List<?> list2 = (List<?>) obj2;
            return combineList(list1, list2);
        }
        else if (obj1 instanceof Map)
        {
            Map<String, Object> childMap1 = (Map<String, Object>) obj1;
            Map<String, Object> childMap2 = (Map<String, Object>) obj2;
            return combine(childMap1, childMap2);
        }

        List<Object> list = new ArrayList<>();
        list.add(obj1);
        list.add(obj2);
        return list;
    }

    private static List<?> combineList(List<?> list1, List<?> list2)
    {
        if (list1 == null)
            return list2;
        else if (list2 == null)
            return list1;

        List<Object> combined = new ArrayList<>();
        combined.addAll(list1);
        combined.addAll(list2);
        return combined;
    }

    private static List<?> appendList(Object obj1, Object obj2)
    {
        if (obj1 instanceof List)
        {
            List<?> list1 = (List<?>) obj1;
            List<Object> list = new ArrayList<>(list1);
            list.add(obj2);
            return list;
        }
        else if (obj2 instanceof List)
        {
            List<?> list2 = (List<?>) obj2;
            List<Object> list = new ArrayList<>(list2);
            list.add(obj1);
            return list;
        }
        else
        {
            List<Object> list = new ArrayList<>();
            list.add(obj1);
            list.add(obj2);
            return list;
        }
    }

    private static boolean isSameType(Object obj1, Object obj2)
    {
        if (obj1 == null || obj2 == null)
            return false;

        return obj1.getClass() == obj2.getClass();
    }
}
