package org.kunlab.scenamatica.scenariofile.structures.inventory;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.GenericInventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;

import java.util.Map;

public class InventoryStructureImpl extends GenericInventoryStructureImpl implements InventoryStructure
{
    public InventoryStructureImpl(Integer size, String title, @NotNull Map<Integer, ItemStackStructure> mainContents)
    {
        super(size, title, mainContents);
    }

    public InventoryStructureImpl(GenericInventoryStructure original)
    {
        super(original.getSize(), original.getTitle(), original.getMainContents());
    }

    @NotNull
    public static Map<String, Object> serialize(@NotNull GenericInventoryStructure structure, @NotNull StructureSerializer serializer)
    {
        return GenericInventoryStructureImpl.serialize(structure, serializer);
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        GenericInventoryStructureImpl.validate(map, serializer);
    }

    @NotNull
    public static InventoryStructureImpl deserialize(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new InventoryStructureImpl(GenericInventoryStructureImpl.deserialize(map, serializer));
    }

    public static InventoryStructureImpl of(@NotNull Inventory inventory)
    {
        return new InventoryStructureImpl(GenericInventoryStructureImpl.of(inventory));
    }

    public static boolean isApplicable(@NotNull Object obj)
    {
        return obj instanceof Inventory;
    }

    @Override
    public void applyTo(Inventory object)
    {
        super.applyToInventory(object);
    }

    @Override
    public boolean isAdequate(Inventory inventory, boolean strict)
    {
        return super.isAdequateInventory(inventory, strict);
    }

    @Override
    public boolean canApplyTo(Object target)
    {
        return isApplicable(target);
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

        // noinspection deprecation  De-Adventure API
        Inventory inventory = Bukkit.createInventory(null, size, title);

        for (Map.Entry<Integer, ItemStackStructure> entry : this.mainContents.entrySet())
            inventory.setItem(entry.getKey(), entry.getValue().create());

        return inventory;
    }
}
