package net.kunmc.lab.scenamatica.scenariofile.beans.inventory;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * プレイヤのインベントリの定義を表すクラスです。
 */
@Value
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PlayerInventoryBean extends InventoryBean implements Serializable
{
    public static final String KEY_MAIN_INVENTORY = "main";
    public static final String KEY_MAIN_HAND = "mainHandItem";
    public static final String KEY_OFF_HAND = "offHandItem";
    public static final String KEY_ARMOR_CONTENTS = "armors";

    /**
     * オフハンドのアイテムを表すクラスです。
     */
    @Nullable
    ItemStackBean mainHand;
    /**
     * オフハンドの定義を表すクラスです。
     */
    @Nullable
    ItemStackBean offHand;

    /**
     * アーマーの定義を表すクラスです。
     */
    @Nullable
    ItemStackBean[] armorContents;

    public PlayerInventoryBean()
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

    public PlayerInventoryBean(@Nullable String title,
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

    public PlayerInventoryBean(@NotNull InventoryBean main, @Nullable ItemStackBean mainHand, @Nullable ItemStackBean offHand, @Nullable ItemStackBean[] armorContents)
    {
        super(9 * 4, main.getTitle(), main.getMainContents());
        this.mainHand = mainHand;
        this.offHand = offHand;
        this.armorContents = armorContents;
    }

    /**
     * プレイヤーインベントリの情報をMapにシリアライズします。
     *
     * @param bean プレイヤーインベントリの情報
     * @return シリアライズされたMap
     */
    public static Map<String, Object> serialize(PlayerInventoryBean bean)
    {
        boolean noArmor = true;
        List<Map<String, Object>> armorContents = new ArrayList<>();
        for (ItemStackBean armorContent : bean.armorContents)
        {
            if (armorContent == null)
                armorContents.add(null);
            else
            {
                armorContents.add(ItemStackBean.serialize(armorContent));
                noArmor = false;
            }
        }

        if (noArmor)
            armorContents = null;

        Map<String, Object> map = new HashMap<>();

        if (!bean.getMainContents().isEmpty())
        {
            Map<String, Object> mainContents = InventoryBean.serialize(bean);
            mainContents.remove(KEY_SIZE);  // Playerのインベントリサイズは36固定なので冗長

            map.put(KEY_MAIN_INVENTORY, mainContents);
        }

        MapUtils.putIfNotNull(map, KEY_ARMOR_CONTENTS, armorContents);

        if (bean.mainHand != null)
            map.put(KEY_MAIN_HAND, ItemStackBean.serialize(bean.mainHand));
        if (bean.offHand != null)
            map.put(KEY_OFF_HAND, ItemStackBean.serialize(bean.offHand));

        return map;
    }

    /**
     * Mapがプレイヤーインベントリの情報を表すMapかどうかを検証します。
     *
     * @param map 検証するMap
     * @throws IllegalArgumentException 必須項目が含まれていない場合か, 型が不正な場合
     */
    public static void validateMap(Map<String, Object> map)
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

            InventoryBean.validateMap(mainInventory);
        }
        if (map.containsKey(KEY_MAIN_HAND))
            ItemStackBean.validateMap(MapUtils.checkAndCastMap(
                    map.get(KEY_MAIN_HAND),
                    String.class, Object.class
            ));
        if (map.containsKey(KEY_OFF_HAND))
            ItemStackBean.validateMap(MapUtils.checkAndCastMap(
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

            ItemStackBean.validateMap(MapUtils.checkAndCastMap(
                            armorContent,
                            String.class, Object.class
                    )
            );
        }
    }

    /**
     * Mapからプレイヤーインベントリの情報をデシリアライズします。
     *
     * @param map デシリアライズするMap
     * @return デシリアライズされたプレイヤーインベントリの情報
     */
    public static PlayerInventoryBean deserialize(Map<String, Object> map)
    {
        validateMap(map);

        ItemStackBean[] armorContents;
        if (map.containsKey(KEY_ARMOR_CONTENTS))
        {
            List<ItemStackBean> armorContentsList = new ArrayList<>();
            for (Object armorContent : (List<?>) map.get(KEY_ARMOR_CONTENTS))
            {
                if (armorContent == null)
                    armorContentsList.add(null);
                else
                    armorContentsList.add(ItemStackBean.deserialize(MapUtils.checkAndCastMap(
                            armorContent,
                            String.class, Object.class
                    )));
            }

            armorContents = armorContentsList.toArray(new ItemStackBean[0]);
        }
        else
            armorContents = new ItemStackBean[4];

        InventoryBean mainInventoryBean;
        if (map.containsKey(KEY_MAIN_INVENTORY))
        {
            Map<String, Object> mainInventory = new HashMap<>(MapUtils.checkAndCastMap(
                    map.get(KEY_MAIN_INVENTORY),
                    String.class, Object.class
            ));

            if (!mainInventory.containsKey(KEY_SIZE))
                mainInventory.put(KEY_SIZE, 9 * 4);

            mainInventoryBean = InventoryBean.deserialize(mainInventory);
        }
        else
            mainInventoryBean = new InventoryBean(9 * 4, null, Collections.emptyMap());


        return new PlayerInventoryBean(
                mainInventoryBean,
                map.containsKey(KEY_MAIN_HAND) ? ItemStackBean.deserialize(MapUtils.checkAndCastMap(
                        map.get(KEY_MAIN_HAND),
                        String.class, Object.class
                )): null,
                map.containsKey(KEY_OFF_HAND) ? ItemStackBean.deserialize(MapUtils.checkAndCastMap(
                        map.get(KEY_OFF_HAND),
                        String.class, Object.class
                )): null,
                armorContents
        );
    }
}
