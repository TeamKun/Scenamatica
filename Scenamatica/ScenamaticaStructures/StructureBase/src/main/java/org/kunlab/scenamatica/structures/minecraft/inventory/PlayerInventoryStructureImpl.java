package org.kunlab.scenamatica.structures.minecraft.inventory;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.GenericInventoryStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.PlayerInventoryStructure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PlayerInventoryStructureImpl extends GenericInventoryStructureImpl implements PlayerInventoryStructure
{
    ItemStackStructure mainHand;
    ItemStackStructure offHand;
    ItemStackStructure[] armorContents;

    public PlayerInventoryStructureImpl()
    {
        super(
                null,
                null,
                Collections.emptyMap()
        );

        this.mainHand = null;
        this.offHand = null;
        this.armorContents = null;
    }

    public PlayerInventoryStructureImpl(@NotNull InventoryStructure main, @Nullable ItemStackStructure mainHand, @Nullable ItemStackStructure offHand, @Nullable ItemStackStructure[] armorContents)
    {
        super(9 * 4, main.getTitle(), main.getMainContents());
        this.mainHand = mainHand;
        this.offHand = offHand;
        this.armorContents = armorContents;
    }

    /**
     * プレイヤインベントリの情報をMapにシリアライズします。
     *
     * @param structure  プレイヤインベントリの情報
     * @param serializer ItemStack のシリアライザ
     * @return シリアライズされたMap
     */
    @NotNull
    public static Map<String, Object> serializePlayerInventory(@NotNull PlayerInventoryStructure structure, @NotNull StructureSerializer serializer)
    {
        boolean noArmor = true;
        List<Map<String, Object>> armorContents = new ArrayList<>();
        for (ItemStackStructure armorContent : structure.getArmorContents())
        {
            if (armorContent == null)
                armorContents.add(null);
            else
            {
                armorContents.add(serializer.serialize(armorContent, ItemStackStructure.class));
                noArmor = false;
            }
        }

        if (noArmor)
            armorContents = null;

        Map<String, Object> map = new HashMap<>();

        if (!structure.getMainContents().isEmpty())
        {
            Map<String, Object> mainContents = serializer.serialize(structure, GenericInventoryStructure.class);
            mainContents.remove(KEY_SIZE);  // Playerのインベントリサイズは36固定なので冗長

            map.put(KEY_MAIN_INVENTORY, mainContents);
        }

        MapUtils.putIfNotNull(map, KEY_ARMOR_CONTENTS, armorContents);

        if (structure.getMainHand() != null)
            map.put(KEY_MAIN_HAND, serializer.serialize(structure.getMainHand(), ItemStackStructure.class));
        if (structure.getOffHand() != null)
            map.put(KEY_OFF_HAND, serializer.serialize(structure.getOffHand(), ItemStackStructure.class));

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        if (map.containsKey(KEY_MAIN_INVENTORY))
        {
            Map<String, Object> mainInventory = new HashMap<>(MapUtils.checkAndCastMap(map.get(KEY_MAIN_INVENTORY)));

            if (!mainInventory.containsKey(KEY_SIZE))
                mainInventory.put(KEY_SIZE, 9 * 4);
            else if (!(mainInventory.get(KEY_SIZE) instanceof Integer
                    || (Integer) mainInventory.get(KEY_SIZE) != 9 * 4))
                throw new IllegalArgumentException(KEY_SIZE + " must be 36 slots in player inventory.");

            serializer.validate(mainInventory, InventoryStructure.class);
        }
        if (map.containsKey(KEY_MAIN_HAND))
            serializer.validate(MapUtils.checkAndCastMap(map.get(KEY_MAIN_HAND)), ItemStackStructure.class);
        if (map.containsKey(KEY_OFF_HAND))
            serializer.validate(MapUtils.checkAndCastMap(map.get(KEY_OFF_HAND)), ItemStackStructure.class);

        if (!map.containsKey(KEY_ARMOR_CONTENTS))
            return;

        if (!(map.get(KEY_ARMOR_CONTENTS) instanceof List))
            throw new IllegalArgumentException(KEY_ARMOR_CONTENTS + " must be List.");
        if (((List<?>) map.get(KEY_ARMOR_CONTENTS)).size() != 4)
            throw new IllegalArgumentException(KEY_ARMOR_CONTENTS + " must be List of size 4.");

        for (Object armorContent : (List<?>) map.get(KEY_ARMOR_CONTENTS))
        {
            if (armorContent == null)
                continue;

            serializer.validate(
                    MapUtils.checkAndCastMap(armorContent),
                    ItemStackStructure.class
            );
        }
    }

    @NotNull
    public static PlayerInventoryStructure deserializePlayerInventory(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        validate(map, serializer);

        ItemStackStructure[] armorContents;
        if (map.containsKey(KEY_ARMOR_CONTENTS))
        {
            List<ItemStackStructure> armorContentsList = new ArrayList<>();
            for (Object armorContent : (List<?>) map.get(KEY_ARMOR_CONTENTS))
            {
                if (armorContent == null)
                    armorContentsList.add(null);
                else
                    armorContentsList.add(serializer.deserialize(MapUtils.checkAndCastMap(armorContent), ItemStackStructure.class));
            }

            armorContents = armorContentsList.toArray(new ItemStackStructure[0]);
        }
        else
            armorContents = new ItemStackStructureImpl[4];

        InventoryStructure mainInventoryStructure;
        if (map.containsKey(KEY_MAIN_INVENTORY))
        {
            Map<String, Object> mainInventory = new HashMap<>(MapUtils.checkAndCastMap(map.get(KEY_MAIN_INVENTORY)));

            mainInventoryStructure = serializer.deserialize(mainInventory, InventoryStructure.class);
        }
        else
            mainInventoryStructure = new InventoryStructureImpl(null, null, Collections.emptyMap());


        ItemStackStructure mainHandItem;
        if (map.containsKey(KEY_MAIN_HAND))
            mainHandItem = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_MAIN_HAND)),
                    ItemStackStructure.class
            );
        else
            mainHandItem = null;

        ItemStackStructure offHandItem;
        if (map.containsKey(KEY_OFF_HAND))
            offHandItem = serializer.deserialize(
                    MapUtils.checkAndCastMap(map.get(KEY_OFF_HAND)),
                    ItemStackStructure.class
            );
        else
            offHandItem = null;

        return new PlayerInventoryStructureImpl(
                mainInventoryStructure,
                mainHandItem,
                offHandItem,
                armorContents
        );
    }

    public static PlayerInventoryStructure of(@NotNull PlayerInventory inventory)
    {
        ItemStackStructure[] armors = new ItemStackStructure[4];
        for (int i = 0; i < armors.length; i++)
        {
            ItemStack armor = inventory.getArmorContents()[i];
            //noinspection ConstantValue  <- nullる可能性ある。
            if (armor == null)
                armors[i] = null;
            else
                armors[i] = ItemStackStructureImpl.of(armor);
        }

        return new PlayerInventoryStructureImpl(
                InventoryStructureImpl.of(inventory),
                ItemStackStructureImpl.of(inventory.getItemInMainHand()),
                ItemStackStructureImpl.of(inventory.getItemInOffHand()),
                armors
        );
    }

    @Override
    public boolean canApplyTo(Object target)
    {
        return target instanceof PlayerInventory;
    }

    @Override
    public void applyTo(PlayerInventory object)
    {
        super.applyToInventory(object);

        if (this.mainHand != null)
            object.setItemInMainHand(this.mainHand.create());

        if (this.offHand != null)
            object.setItemInOffHand(this.offHand.create());

        if (this.armorContents != null)
        {
            ItemStack[] armorContents = new ItemStack[4];
            for (int i = 0; i < this.armorContents.length; i++)
                if (this.armorContents[i] != null)
                    armorContents[3 - i] =  // 逆順にする => 0: ヘルメット ～ 3: ブーツ で統一するため(Bukkitは逆)
                            this.armorContents[i].create();

            object.setArmorContents(armorContents);
        }
    }

    @Override
    public boolean isAdequate(PlayerInventory playerInventory, boolean strict)
    {
        if (!super.isAdequateInventory(playerInventory, strict))
            return false;

        ItemStackStructure[] expectedArmors = this.armorContents;
        ItemStack[] actualArmors = playerInventory.getArmorContents();

        if (expectedArmors != null)
            for (int i = 0; i < expectedArmors.length; i++)
                if (!(expectedArmors[i] == null || expectedArmors[i].isAdequate(actualArmors[i], strict)))
                    return false;

        ItemStackStructure expectedMainHand = this.mainHand;
        ItemStack actualMainHand = playerInventory.getItemInMainHand();
        if (!(expectedMainHand == null || expectedMainHand.isAdequate(actualMainHand, strict)))
            return false;

        ItemStackStructure expectedOffHand = this.offHand;
        ItemStack actualOffHand = playerInventory.getItemInOffHand();
        return expectedOffHand == null || expectedOffHand.isAdequate(actualOffHand, strict);
    }

    @Override
    public PlayerInventory create()
    {
        return null;
    }
}
