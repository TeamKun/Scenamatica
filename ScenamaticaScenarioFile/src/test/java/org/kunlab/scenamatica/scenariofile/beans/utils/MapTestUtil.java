package org.kunlab.scenamatica.scenariofile.beans.utils;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Assertions;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UtilityClass
public class MapTestUtil
{
    public static void assertEqual(Map<?, ?> expected, Map<?, ?> actual)
    {
        assertEquals(expected.size(), actual.size(), "Map size is not equal.");
        for (Map.Entry<?, ?> entry : expected.entrySet())
        {
            Assertions.assertTrue(actual.containsKey(entry.getKey()), "Map key of " + entry.getKey() + " is not found.");

            if (entry.getValue() instanceof Map)
            {
                assertEqual((Map<?, ?>) entry.getValue(), (Map<?, ?>) actual.get(entry.getKey()));
                continue;
            }
            else if (entry.getValue() instanceof List)
            {
                assertEqual((List<?>) entry.getValue(), (List<?>) actual.get(entry.getKey()));
                continue;
            }
            assertEquals(entry.getValue(), actual.get(entry.getKey()), "Map value of key " + entry.getKey() + " is not equal.");
        }
    }

    public static void assertEqual(List<?> expected, List<?> actual)
    {
        assertEquals(expected.size(), actual.size(), "List size is not equal.");
        for (int i = 0; i < expected.size(); i++)
        {
            if (expected.get(i) instanceof Map)
            {
                assertEqual((Map<?, ?>) expected.get(i), (Map<?, ?>) actual.get(i));
                continue;
            }
            else if (expected.get(i) instanceof List)
            {
                assertEqual((List<?>) expected.get(i), (List<?>) actual.get(i));
                continue;
            }
            assertEquals(expected.get(i), actual.get(i), "List value of index " + i + " is not equal.");
        }
    }
}
