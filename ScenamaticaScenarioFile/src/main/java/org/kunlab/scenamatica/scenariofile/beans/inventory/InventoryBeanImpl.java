package org.kunlab.scenamatica.scenariofile.beans.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
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
    private final Integer size;
    private final String title;
    @NotNull
    private final Map<Integer, ItemStackBean> mainContents;

    public InventoryBeanImpl()
    {
        this(
                null,
                null,
                Collections.emptyMap()
        );
    }

    @NotNull
    public static Map<String, Object> serialize(@NotNull InventoryBean bean, @NotNull BeanSerializer serializer)
    {
        Map<Integer, Object> contents = new HashMap<>();
        for (Map.Entry<Integer, ItemStackBean> entry : bean.getMainContents().entrySet())
            contents.put(entry.getKey(), serializer.serialize(entry.getValue(), ItemStackBean.class));

        Map<String, Object> map = new HashMap<>();
        MapUtils.putIfNotNull(map, KEY_SIZE, bean.getSize());
        MapUtils.putIfNotNull(map, KEY_TITLE, bean.getTitle());
        MapUtils.putMapIfNotEmpty(map, KEY_MAIN_CONTENTS, contents);
        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        MapUtils.checkTypeIfContains(map, KEY_SIZE, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_TITLE, String.class);

        if (!map.containsKey(KEY_MAIN_CONTENTS))
            return;

        Map<Integer, Object> contents = MapUtils.checkAndCastMap(
                map.get(KEY_MAIN_CONTENTS),
                Integer.class,
                Object.class
        );

        for (Map.Entry<Integer, Object> entry : contents.entrySet())
            serializer.validate(MapUtils.checkAndCastMap(entry.getValue()), ItemStackBean.class);
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
                        serializer.deserialize(MapUtils.checkAndCastMap(entry.getValue()), ItemStackBean.class)
                );
        }

        return new InventoryBeanImpl(
                MapUtils.getOrNull(map, KEY_SIZE),
                MapUtils.getOrNull(map, KEY_TITLE),
                mainContents
        );
    }

    @Override
    public Inventory createInventory()
    {
        Integer size = this.size;
        if (size == null)
            size = 9 * 4; // プレイヤインベントリのサイズ

        Component title;
        if (this.title == null)
            title = Component.empty();
        else
            title = Component.text(this.title);

        Inventory inventory = Bukkit.createInventory(null, size, title);

        for (Map.Entry<Integer, ItemStackBean> entry : this.mainContents.entrySet())
            inventory.setItem(entry.getKey(), entry.getValue().toItemStack());

        return inventory;
    }
}
