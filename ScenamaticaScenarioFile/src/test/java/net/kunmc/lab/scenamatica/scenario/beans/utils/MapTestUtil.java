package net.kunmc.lab.scenamatica.scenario.beans.utils;

import lombok.experimental.UtilityClass;
import org.junit.jupiter.api.Assertions;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@UtilityClass
public class MapTestUtil
{
    public static void assertEqual(Map<String, Object> expected, Map<String, Object> actual)
    {
        assertEquals(expected.size(), actual.size());
        for (Map.Entry<String, Object> entry : expected.entrySet())
        {
            Assertions.assertTrue(actual.containsKey(entry.getKey()));
            assertEquals(entry.getValue(), actual.get(entry.getKey()));
        }
    }
}
