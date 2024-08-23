package org.kunlab.scenamatica.structures.minecraft.inventory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class InventoryStructureImpl implements InventoryStructure
{
    protected final Integer size;
    protected final String title;
    @NotNull
    protected final Map<Integer, ItemStackStructure> mainContents;

    protected InventoryStructureImpl()
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

    public static InventoryStructure of(@NotNull Inventory inventory)
    {
        Map<Integer, ItemStackStructure> mainContents = new HashMap<>();
        for (int i = 0; i < inventory.getSize(); i++)
        {
            ItemStack item = inventory.getItem(i);
            if (item == null)
                continue;

            mainContents.put(i, ItemStackStructureImpl.of(item));
        }

        return new InventoryStructureImpl(
                inventory.getSize(),
                null,
                mainContents
        );
    }

    public static boolean isApplicableInventory(@NotNull Object target)
    {
        return target instanceof Inventory;
    }

    public boolean isAdequate(Inventory inventory, boolean strict)
    {
        if (strict && !(this.size == null || this.size == inventory.getSize()))
            return false;


        for (int i = 0; i < inventory.getSize(); i++)
        {
            ItemStackStructure expected = this.mainContents.get(i);
            ItemStack actual = inventory.getItem(i);

            if (!(expected == null || expected.isAdequate(actual, strict)))
                return false;
        }

        return true;
    }
    @Override
    public void applyTo(Inventory inventory)
    {
        for (Map.Entry<Integer, ItemStackStructure> entry : this.mainContents.entrySet())
        {
            int idx = entry.getKey();
            if (idx < 0 || idx >= inventory.getSize())
                throw new IllegalArgumentException("Cannot apply inventory structure to inventory: item index out of range: " + idx + " (inventory size: " + inventory.getSize() + ")");

            if (entry.getValue() == null)
                inventory.setItem(idx, null);
            else
                inventory.setItem(idx, entry.getValue().create());
        }
    }

    @Override
    public Inventory create()
    {
        Integer size = this.size;
        if (size == null)
            size = 9 * 4; // プレイヤインベントリのサイズ

        String title = this.title;
        if (title == null)
            title = "";

        Inventory inventory = Bukkit.createInventory(null, size, title);

        for (Map.Entry<Integer, ItemStackStructure> entry : this.mainContents.entrySet())
            inventory.setItem(entry.getKey(), entry.getValue().create());

        return inventory;
    }

    @Override
    public boolean canApplyTo(Object target)
    {
        return target instanceof Inventory;
    }
}
