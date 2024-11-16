package org.kunlab.scenamatica.structures.minecraft.inventory;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;
import org.kunlab.scenamatica.structures.StructureValidators;

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

    public static void validate(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        node.get(KEY_SIZE).ensureTypeOfIfExists(YAMLNodeType.INTEGER);
        node.get(KEY_TITLE).ensureTypeOfIfExists(YAMLNodeType.STRING);
        if (!node.containsKey(KEY_MAIN_CONTENTS))
            return;

        StructuredYamlNode contentsNode = node.get(KEY_MAIN_CONTENTS);
        contentsNode.validateIfExists(StructureValidators.mapType(
                n -> n.isType(YAMLNodeType.STRING),
                n -> {
                    serializer.validate(n, ItemStackStructure.class);
                    return null;
                }
        ));
    }

    @NotNull
    public static InventoryStructure deserialize(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        validate(node, serializer);

        Integer size = node.get(KEY_SIZE).asInteger(null);
        String title = node.get(KEY_TITLE).asString(null);

        Map<Integer, ItemStackStructure> mainContents = new HashMap<>();
        if (node.containsKey(KEY_MAIN_CONTENTS))
        {
            StructuredYamlNode contentsNode = node.get(KEY_MAIN_CONTENTS);
            mainContents = contentsNode.asMap(
                    StructuredYamlNode::asInteger,
                    n -> serializer.deserialize(n, ItemStackStructure.class)
            );
        }

        return new InventoryStructureImpl(
                size,
                title,
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

    public boolean isAdequate(@Nullable Inventory inventory, boolean strict)
    {
        if (inventory == null)
            return false;

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
    public void applyTo(@NotNull Inventory inventory)
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
    public boolean canApplyTo(@Nullable Object target)
    {
        return target instanceof Inventory;
    }
}
