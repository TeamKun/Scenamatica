package org.kunlab.scenamatica.commons.utils;

import org.bukkit.Material;
import org.bukkit.block.data.BlockData;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class BlockDataParser
{
    public static Map<String, Object> toMap(BlockData data)
    {
        if (data == null)
            return Collections.emptyMap();

        String dataString = data.getAsString(true);
        return parseSpecifiers(trimSpecifiers(dataString));
    }

    public static BlockData fromMap(Material material, Map<String, Object> map)
    {
        if (map == null)
            return material.createBlockData();

        String specifierString = mapToSpecifierString(map);
        return material.createBlockData(specifierString);
    }

    private static String mapToSpecifierString(Map<String, Object> map)
    {
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet())
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append(",");
        return builder.toString();
    }

    private static String trimSpecifiers(String specifiers)
    {
        int start = specifiers.indexOf('[');
        int end = specifiers.lastIndexOf(']');

        return specifiers.substring(start + 1, end);
    }

    private static Map<String, Object> parseSpecifiers(String specifiers)
    {
        Map<String, Object> map = new HashMap<>();

        String buffer;
        int idx;
        while ((idx = specifiers.indexOf('=')) != -1)
        {
            buffer = specifiers.substring(0, idx);
            specifiers = specifiers.substring(idx + 1);

            idx = specifiers.indexOf(',');
            if (idx == -1)
                idx = specifiers.length();

            String key = buffer.trim();
            String value = specifiers.substring(0, idx).trim();

            specifiers = specifiers.substring(idx + 1);

            Object casted = castType(value);
            map.put(key, casted);
        }

        return map;
    }

    private static Object castType(String value)
    {
        if (value.equalsIgnoreCase("true"))
            return true;
        else if (value.equalsIgnoreCase("false"))
            return false;
        else if (value.matches("-?\\d+"))
        {
            try
            {
                return Integer.parseInt(value);
            }
            catch (NumberFormatException e)
            {
                return Long.parseLong(value);
            }
        }
        else if (value.matches("-?\\d+\\.\\d+"))
            return Double.parseDouble(value);
        else
            return value;
    }

}
