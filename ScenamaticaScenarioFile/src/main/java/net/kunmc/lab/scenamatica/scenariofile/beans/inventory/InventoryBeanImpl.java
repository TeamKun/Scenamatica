package net.kunmc.lab.scenamatica.scenariofile.beans.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class InventoryBeanImpl implements InventoryBean
{
    private final int size;
    @Nullable
    private final String title;
    @NotNull
    private final Map<Integer, ItemStackBean> mainContents;

    public InventoryBeanImpl()
    {
        this(
                0,
                null,
                Collections.emptyMap()
        );
    }

    public static Map<String, Object> serialize(InventoryBean bean)
    {
        Map<Integer, Object> contents = new HashMap<>();
        for (Map.Entry<Integer, ItemStackBean> entry : bean.getMainContents().entrySet())
            contents.put(entry.getKey(), ItemStackBeanImpl.serialize(entry.getValue()));

        Map<String, Object> map = new HashMap<>();
        map.put(KEY_SIZE, bean.getSize());
        MapUtils.putIfNotNull(map, KEY_TITLE, bean.getTitle());
        MapUtils.putMapIfNotEmpty(map, KEY_MAIN_CONTENTS, contents);
        return map;
    }

    public static void validate(Map<String, Object> map)
    {
        MapUtils.checkType(map, KEY_SIZE, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_TITLE, String.class);

        if (!map.containsKey(KEY_MAIN_CONTENTS))
            return;

        Map<Integer, Object> contents = MapUtils.checkAndCastMap(
                map.get(KEY_MAIN_CONTENTS),
                Integer.class,
                Object.class
        );

        for (Map.Entry<Integer, Object> entry : contents.entrySet())
            ItemStackBeanImpl.validate(MapUtils.checkAndCastMap(
                    entry.getValue(),
                    String.class,
                    Object.class
            ));
    }

    public static InventoryBean deserialize(Map<String, Object> map)
    {
        validate(map);

        Map<Integer, ItemStackBean> mainContents = new HashMap<>();
        if (map.containsKey(KEY_MAIN_CONTENTS))
        {
            Map<Integer, Object> contents = MapUtils.checkAndCastMap(
                    map.get(KEY_MAIN_CONTENTS),
                    Integer.class,
                    Object.class

            );

            for (Map.Entry<Integer, Object> entry : contents.entrySet())
                mainContents.put(
                        entry.getKey(),
                        ItemStackBeanImpl.deserialize(MapUtils.checkAndCastMap(
                                entry.getValue(),
                                String.class,
                                Object.class
                        ))
                );
        }

        return new InventoryBeanImpl(
                (int) map.get(KEY_SIZE),
                MapUtils.getOrNull(map, KEY_TITLE),
                mainContents
        );
    }
}
