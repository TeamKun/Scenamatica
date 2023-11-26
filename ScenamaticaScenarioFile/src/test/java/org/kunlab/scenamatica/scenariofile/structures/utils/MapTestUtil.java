package org.kunlab.scenamatica.scenariofile.structures.utils;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Assertions;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UtilityClass
public class MapTestUtil
{
    public static void assertEqual(Map<?, ?> expected, Map<?, ?> acutual)
    {
        assertEqual(expected, acutual, 0);
    }

    public static void assertEqual(List<?> expected, List<?> acutual)
    {
        assertEqual(expected, acutual, 0);
    }

    public static void assertEqual(Map<?, ?> expected, Map<?, ?> actual, int depth)
    {
        assertEquals(expected.size(), actual.size(), () -> {
            String builder = "Map size is not equal.\n" +
                    "Expected: " + expected + "\n" +
                    "Actual: " + actual + "\n-----\n";
            dumpMap("Expected", expected, depth);
            dumpMap("Actual", actual, depth);

            return builder;
        });

        for (Map.Entry<?, ?> entry : expected.entrySet())
        {
            Assertions.assertTrue(actual.containsKey(entry.getKey()), "Map key of " + entry.getKey() + " is not found.");

            if (entry.getValue() instanceof Map)
            {
                assertEqual((Map<?, ?>) entry.getValue(), (Map<?, ?>) actual.get(entry.getKey()), depth + 1);
                continue;
            }
            else if (entry.getValue() instanceof List)
            {
                assertEqual((List<?>) entry.getValue(), (List<?>) actual.get(entry.getKey()), depth + 1);
                continue;
            }
            assertEquals(entry.getValue(), actual.get(entry.getKey()), "Map value of key " + entry.getKey() + " is not equal.");
        }
    }

    public static void assertEqual(List<?> expected, List<?> actual, int depth)
    {
        assertEquals(expected.size(), actual.size(), "List size is not equal.");
        for (int i = 0; i < expected.size(); i++)
        {
            if (expected.get(i) instanceof Map)
            {
                assertEqual((Map<?, ?>) expected.get(i), (Map<?, ?>) actual.get(i), depth + 1);
                continue;
            }
            else if (expected.get(i) instanceof List)
            {
                assertEqual((List<?>) expected.get(i), (List<?>) actual.get(i), depth + 1);
                continue;
            }
            assertEquals(expected.get(i), actual.get(i), "List value of index " + i + " is not equal.");
        }
    }

    public void dumpMap(String name, Map<?, ?> map, int depth)
    {
        int indents = depth * 2;
        char[] indentChars = new char[indents];
        Arrays.fill(indentChars, ' ');
        String indent = new String(indentChars);

        if (name != null)
            System.out.println(indent + name + ":");

        for (Map.Entry<?, ?> entry : map.entrySet())
        {
            if (entry.getValue() instanceof Map)
            {
                dumpMap(entry.getKey().toString(), (Map<?, ?>) entry.getValue(), depth + 1);
                continue;
            }
            else if (entry.getValue() instanceof List)
            {
                dumpList(entry.getKey().toString(), (List<?>) entry.getValue(), depth + 1);
                continue;
            }

            System.out.println(indent + "- " + entry.getKey() + ": " + entry.getValue());
        }

    }

    public void dumpList(String name, List<?> list, int depth)
    {
        int indents = depth * 2;
        char[] indentChars = new char[indents];
        Arrays.fill(indentChars, ' ');
        String indent = new String(indentChars);

        if (name != null)
            System.out.println(indent + name + ":");

        for (int i = 0; i < list.size(); i++)
        {
            if (list.get(i) instanceof Map)
            {
                dumpMap(name, (Map<?, ?>) list.get(i), depth + 1);
                continue;
            }
            else if (list.get(i) instanceof List)
            {
                dumpList(null, (List<?>) list.get(i), depth + 1);
                continue;
            }

            System.out.println(indent + "- [" + i + "]: " + list.get(i));

        }
    }
}
