package org.kunlab.scenamatica.structures.minecraft.inventory;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
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
public class PlayerInventoryStructureImpl extends InventoryStructureImpl implements PlayerInventoryStructure
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
            Map<String, Object> mainContents = serializer.serialize(structure, InventoryStructure.class);
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

    public static void validatePlayerInventory(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        if (node.containsKey(KEY_MAIN_INVENTORY))
        {
            StructuredYamlNode mainInventoryNode = node.get(KEY_MAIN_INVENTORY);

            StructuredYamlNode sizeNode = mainInventoryNode.get(KEY_SIZE);
            if (!(sizeNode.isNullish() || sizeNode.asInteger() == 36))
                throw new IllegalArgumentException(KEY_SIZE + " must be 36 slots in player inventory.");

            serializer.validate(mainInventoryNode, InventoryStructure.class);
        }
        if (node.containsKey(KEY_MAIN_HAND))
            serializer.validate(node.get(KEY_MAIN_HAND), ItemStackStructure.class);
        if (node.containsKey(KEY_OFF_HAND))
            serializer.validate(node.get(KEY_OFF_HAND), ItemStackStructure.class);

        if (!node.containsKey(KEY_ARMOR_CONTENTS))
            return;

        if (!node.get(KEY_ARMOR_CONTENTS).isType(YAMLNodeType.LIST))
            throw new IllegalArgumentException(KEY_ARMOR_CONTENTS + " must be List.");
        if (node.get(KEY_ARMOR_CONTENTS).size() != 4)
            throw new IllegalArgumentException(KEY_ARMOR_CONTENTS + " must be List of size 4.");

        for (StructuredYamlNode armorContent : node.get(KEY_ARMOR_CONTENTS).asList())
        {
            if (armorContent.isNullish())
                continue;

            serializer.validate(armorContent, ItemStackStructure.class);
        }
    }

    @NotNull
    public static PlayerInventoryStructure deserializePlayerInventory(@NotNull StructuredYamlNode node, @NotNull StructureSerializer serializer) throws YamlParsingException
    {
        validatePlayerInventory(node, serializer);

        ItemStackStructure[] armorContents;
        if (node.containsKey(KEY_ARMOR_CONTENTS))
        {
            List<ItemStackStructure> armorContentsList = new ArrayList<>();
            for (StructuredYamlNode armorContent : node.get(KEY_ARMOR_CONTENTS).asList())
            {
                if (armorContent.isNullish())
                    armorContentsList.add(null);
                else
                    armorContentsList.add(serializer.deserialize(armorContent, ItemStackStructure.class));
            }

            armorContents = armorContentsList.toArray(new ItemStackStructure[0]);
        }
        else
            armorContents = new ItemStackStructureImpl[4];

        InventoryStructure mainInventoryStructure;
        if (node.containsKey(KEY_MAIN_INVENTORY))
        {
            StructuredYamlNode mainInventoryNode = node.get(KEY_MAIN_INVENTORY);
            mainInventoryStructure = serializer.deserialize(mainInventoryNode, InventoryStructure.class);
        }
        else
            mainInventoryStructure = new InventoryStructureImpl(null, null, Collections.emptyMap());


        ItemStackStructure mainHandItem;
        if (node.containsKey(KEY_MAIN_HAND))
            mainHandItem = serializer.deserialize(
                    node.get(KEY_MAIN_HAND),
                    ItemStackStructure.class
            );
        else
            mainHandItem = null;

        ItemStackStructure offHandItem;
        if (node.containsKey(KEY_OFF_HAND))
            offHandItem = serializer.deserialize(
                    node.get(KEY_OFF_HAND),
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

    public static PlayerInventoryStructure ofPlayerInventory(@NotNull PlayerInventory inventory)
    {
        ItemStackStructure[] armors = new ItemStackStructure[4];
        for (int i = 0; i < armors.length; i++)
        {
            ItemStack armor = inventory.getArmorContents()[i];
            if (armor == null || armor.getType().isEmpty())
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

    public static boolean isApplicablePlayerInventory(@NotNull Object target)
    {
        return target instanceof PlayerInventory;
    }

    @Override
    public boolean canApplyTo(@Nullable Object target)
    {
        return target instanceof PlayerInventory;
    }

    @Override
    public void applyTo(@NotNull Inventory inventory)
    {
        super.applyTo(inventory);
        if (!(inventory instanceof PlayerInventory))
            return;

        PlayerInventory playerInventory = (PlayerInventory) inventory;

        if (this.mainHand != null)
            playerInventory.setItemInMainHand(this.mainHand.create());

        if (this.offHand != null)
            playerInventory.setItemInOffHand(this.offHand.create());

        if (this.armorContents != null)
        {
            ItemStack[] armorContents = new ItemStack[4];
            for (int i = 0; i < this.armorContents.length; i++)
                if (this.armorContents[i] != null)
                    armorContents[3 - i] =  // 逆順にする => 0: ヘルメット ～ 3: ブーツ で統一するため(Bukkitは逆)
                            this.armorContents[i].create();

            playerInventory.setArmorContents(armorContents);
        }
    }

    @Override
    public boolean isAdequate(@Nullable Inventory inventory, boolean strict)
    {
        if (!(super.isAdequate(inventory, strict) && inventory instanceof PlayerInventory))
            return false;

        PlayerInventory playerInventory = (PlayerInventory) inventory;

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
        throw new UnsupportedOperationException();
    }
}
