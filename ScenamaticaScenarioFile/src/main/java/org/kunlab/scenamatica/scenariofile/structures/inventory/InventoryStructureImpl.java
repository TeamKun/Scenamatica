package org.kunlab.scenamatica.scenariofile.structures.inventory;

import lombok.AllArgsConstructor;
import lombok.Data;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class InventoryStructureImpl implements InventoryStructure
{
    private final Integer size;
    private final String title;
    @NotNull
    private final Map<Integer, ItemStackStructure> mainContents;

    public InventoryStructureImpl()
    {
        this(
                null,
                null,
                Collections.emptyMap()
        );
    }

    @NotNull
    public static Map<String, Object> serialize(@NotNull InventoryStructure structure, @NotNull StructureSerializer serializer)
    {
        Map<Integer, Object> contents = new HashMap<>();
        for (Map.Entry<Integer, ItemStackStructure> entry : structure.getMainContents().entrySet())
            contents.put(entry.getKey(), serializer.serialize(entry.getValue(), ItemStackStructure.class));

        Map<String, Object> map = new HashMap<>();
        MapUtils.putIfNotNull(map, KEY_SIZE, structure.getSize());
        MapUtils.putIfNotNull(map, KEY_TITLE, structure.getTitle());
        MapUtils.putMapIfNotEmpty(map, KEY_MAIN_CONTENTS, contents);
        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
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
            serializer.validate(MapUtils.checkAndCastMap(entry.getValue()), ItemStackStructure.class);
    }

    @NotNull
    public static InventoryStructure deserialize(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validate(map, serializer);

        Map<Integer, ItemStackStructure> mainContents = new HashMap<>();
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
                        serializer.deserialize(MapUtils.checkAndCastMap(entry.getValue()), ItemStackStructure.class)
                );
        }

        return new InventoryStructureImpl(
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

        for (Map.Entry<Integer, ItemStackStructure> entry : this.mainContents.entrySet())
            inventory.setItem(entry.getKey(), entry.getValue().toItemStack());

        return inventory;
    }
}
