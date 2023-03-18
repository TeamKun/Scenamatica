package net.kunmc.lab.scenamatica.scenario.beans.utils;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Assertions;

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

            assertEquals(entry.getValue(), actual.get(entry.getKey()), "Map value of key " + entry.getKey() + " is not equal.");
        }
    }
}
