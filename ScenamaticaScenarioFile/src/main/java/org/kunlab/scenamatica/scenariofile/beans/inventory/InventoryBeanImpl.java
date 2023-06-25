package org.kunlab.scenamatica.scenariofile.beans.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;

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

    @NotNull
    public static Map<String, Object> serialize(@NotNull InventoryBean bean, @NotNull BeanSerializer serializer)
    {
        Map<Integer, Object> contents = new HashMap<>();
        for (Map.Entry<Integer, ItemStackBean> entry : bean.getMainContents().entrySet())
            contents.put(entry.getKey(), serializer.serializeItemStack(entry.getValue()));

        Map<String, Object> map = new HashMap<>();
        map.put(KEY_SIZE, bean.getSize());
        MapUtils.putIfNotNull(map, KEY_TITLE, bean.getTitle());
        MapUtils.putMapIfNotEmpty(map, KEY_MAIN_CONTENTS, contents);
        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
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
            serializer.validateItemStack(MapUtils.checkAndCastMap(
                    entry.getValue(),
                    String.class,
                    Object.class
            ));
    }

    @NotNull
    public static InventoryBean deserialize(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        validate(map, serializer);

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
                        serializer.deserializeItemStack(MapUtils.checkAndCastMap(
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
