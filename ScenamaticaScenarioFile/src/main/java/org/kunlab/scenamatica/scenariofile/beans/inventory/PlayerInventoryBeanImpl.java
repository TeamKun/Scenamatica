package org.kunlab.scenamatica.scenariofile.beans.inventory;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryBean;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PlayerInventoryBeanImpl extends InventoryBeanImpl implements PlayerInventoryBean
{
    @Nullable
    ItemStackBean mainHand;
    @Nullable
    ItemStackBean offHand;

    @Nullable
    ItemStackBean[] armorContents;

    public PlayerInventoryBeanImpl()
    {
        super(
                0,
                null,
                Collections.emptyMap()
        );

        this.mainHand = null;
        this.offHand = null;
        this.armorContents = null;
    }

    public PlayerInventoryBeanImpl(@Nullable String title,
                                   @NotNull Map<Integer, ItemStackBean> mainContents,
                                   @Nullable ItemStackBean mainHand,
                                   @Nullable ItemStackBean offHand,
                                   @Nullable ItemStackBean[] armorContents
    )
    {
        super(9 * 4, title, mainContents);
        this.mainHand = mainHand;
        this.offHand = offHand;
        this.armorContents = armorContents;
    }

    public PlayerInventoryBeanImpl(@NotNull InventoryBean main, @Nullable ItemStackBean mainHand, @Nullable ItemStackBean offHand, @Nullable ItemStackBean[] armorContents)
    {
        super(9 * 4, main.getTitle(), main.getMainContents());
        this.mainHand = mainHand;
        this.offHand = offHand;
        this.armorContents = armorContents;
    }

    /**
     * プレイヤーインベントリの情報をMapにシリアライズします。
     *
     * @param bean       プレイヤーインベントリの情報
     * @param serializer ItemStack のシリアライザ
     * @return シリアライズされたMap
     */
    @NotNull
    public static Map<String, Object> serialize(@NotNull PlayerInventoryBean bean, @NotNull BeanSerializer serializer)
    {
        boolean noArmor = true;
        List<Map<String, Object>> armorContents = new ArrayList<>();
        for (ItemStackBean armorContent : bean.getArmorContents())
        {
            if (armorContent == null)
                armorContents.add(null);
            else
            {
                armorContents.add(serializer.serializeItemStack(armorContent));
                noArmor = false;
            }
        }

        if (noArmor)
            armorContents = null;

        Map<String, Object> map = new HashMap<>();

        if (!bean.getMainContents().isEmpty())
        {
            Map<String, Object> mainContents = serializer.serializeInventory(bean);
            mainContents.remove(KEY_SIZE);  // Playerのインベントリサイズは36固定なので冗長

            map.put(KEY_MAIN_INVENTORY, mainContents);
        }

        MapUtils.putIfNotNull(map, KEY_ARMOR_CONTENTS, armorContents);

        if (bean.getMainHand() != null)
            map.put(KEY_MAIN_HAND, serializer.serializeItemStack(bean.getMainHand()));
        if (bean.getOffHand() != null)
            map.put(KEY_OFF_HAND, serializer.serializeItemStack(bean.getOffHand()));

        return map;
    }

    public static void validate(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        if (map.containsKey(KEY_MAIN_INVENTORY))
        {
            Map<String, Object> mainInventory = new HashMap<>(MapUtils.checkAndCastMap(
                    map.get(KEY_MAIN_INVENTORY),
                    String.class, Object.class
            ));

            if (!mainInventory.containsKey(KEY_SIZE))
                mainInventory.put(KEY_SIZE, 9 * 4);
            else if (!(mainInventory.get(KEY_SIZE) instanceof Integer ||
                    (Integer) mainInventory.get(KEY_SIZE) != 9 * 4))
                throw new IllegalArgumentException(KEY_SIZE + " must be 36 slots in player inventory.");

            serializer.validateInventory(mainInventory);
        }
        if (map.containsKey(KEY_MAIN_HAND))
            serializer.validateItemStack(MapUtils.checkAndCastMap(
                    map.get(KEY_MAIN_HAND),
                    String.class, Object.class
            ));
        if (map.containsKey(KEY_OFF_HAND))
            serializer.validateItemStack(MapUtils.checkAndCastMap(
                    map.get(KEY_OFF_HAND),
                    String.class, Object.class
            ));

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

            serializer.validateItemStack(MapUtils.checkAndCastMap(
                            armorContent,
                            String.class, Object.class
                    )
            );
        }
    }

    @NotNull
    public static PlayerInventoryBean deserialize(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        validate(map, serializer);

        ItemStackBean[] armorContents;
        if (map.containsKey(KEY_ARMOR_CONTENTS))
        {
            List<ItemStackBean> armorContentsList = new ArrayList<>();
            for (Object armorContent : (List<?>) map.get(KEY_ARMOR_CONTENTS))
            {
                if (armorContent == null)
                    armorContentsList.add(null);
                else
                    armorContentsList.add(serializer.deserializeItemStack(MapUtils.checkAndCastMap(
                            armorContent,
                            String.class, Object.class
                    )));
            }

            armorContents = armorContentsList.toArray(new ItemStackBean[0]);
        }
        else
            armorContents = new ItemStackBeanImpl[4];

        InventoryBean mainInventoryBean;
        if (map.containsKey(KEY_MAIN_INVENTORY))
        {
            Map<String, Object> mainInventory = new HashMap<>(MapUtils.checkAndCastMap(
                    map.get(KEY_MAIN_INVENTORY),
                    String.class, Object.class
            ));

            if (!mainInventory.containsKey(KEY_SIZE))
                mainInventory.put(KEY_SIZE, 9 * 4);

            mainInventoryBean = serializer.deserializeInventory(mainInventory);
        }
        else
            mainInventoryBean = new InventoryBeanImpl(9 * 4, null, Collections.emptyMap());


        ItemStackBean mainHandItem;
        if (map.containsKey(KEY_MAIN_HAND))
            mainHandItem = serializer.deserializeItemStack(MapUtils.checkAndCastMap(
                    map.get(KEY_MAIN_HAND),
                    String.class, Object.class
            ));
        else
            mainHandItem = null;

        ItemStackBean offHandItem;
        if (map.containsKey(KEY_OFF_HAND))
            offHandItem = serializer.deserializeItemStack(MapUtils.checkAndCastMap(
                    map.get(KEY_OFF_HAND),
                    String.class, Object.class
            ));
        else
            offHandItem = null;

        return new PlayerInventoryBeanImpl(
                mainInventoryBean,
                mainHandItem,
                offHandItem,
                armorContents
        );
    }
}
