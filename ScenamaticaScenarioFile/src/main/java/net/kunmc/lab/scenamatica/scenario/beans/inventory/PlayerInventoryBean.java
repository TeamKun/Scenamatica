package net.kunmc.lab.scenamatica.scenario.beans.inventory;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * プレイヤのインベントリの定義を表すクラスです。
 */
@Value
public class PlayerInventoryBean implements Serializable
{
    private static final String KEY_MAIN_INVENTORY = "main";
    private static final String KEY_OFF_HAND = "offHand";
    private static final String KEY_ARMOR_CONTENTS = "armors";

    /**
     * メインインベントリの定義を表すクラスです。
     */
    @NotNull
    InventoryBean mainInventory;

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

    /**
     * プレイヤーインベントリの情報をMapにシリアライズします。
     *
     * @param bean プレイヤーインベントリの情報
     * @return シリアライズされたMap
     */
    public static Map<String, Object> serialize(PlayerInventoryBean bean)
    {
        List<Map<String, Object>> armorContents = new ArrayList<>();
        for (ItemStackBean armorContent : bean.armorContents)
        {
            if (armorContent == null)
                armorContents.add(null);
            else
                armorContents.add(ItemStackBean.serialize(armorContent));
        }

        Map<String, Object> map = new HashMap<>();
        map.put(KEY_MAIN_INVENTORY, InventoryBean.serialize(bean.mainInventory));
        MapUtils.putIfNotNull(map, KEY_ARMOR_CONTENTS, armorContents);

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
        if (!map.containsKey(KEY_MAIN_INVENTORY))
            throw new IllegalArgumentException(KEY_MAIN_INVENTORY + " is required.");

        InventoryBean.validateMap(MapUtils.checkAndCastMap(
                        map.get(KEY_MAIN_INVENTORY),
                        String.class, Object.class
                )
        );

        if (map.containsKey(KEY_OFF_HAND))
            ItemStackBean.validateMap(MapUtils.checkAndCastMap(
                            map.get(KEY_OFF_HAND),
                            String.class, Object.class
                    )
            );

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

        List<ItemStackBean> armorContents = new ArrayList<>();
        for (Object armorContent : (List<?>) map.get(KEY_ARMOR_CONTENTS))
        {
            if (armorContent == null)
                armorContents.add(null);
            else
                armorContents.add(ItemStackBean.deserialize(MapUtils.checkAndCastMap(
                                armorContent,
                                String.class, Object.class
                        )
                ));
        }

        return new PlayerInventoryBean(
                InventoryBean.deserialize(MapUtils.checkAndCastMap(
                                map.get(KEY_MAIN_INVENTORY),
                                String.class, Object.class
                        )
                ),
                map.containsKey(KEY_OFF_HAND) ? ItemStackBean.deserialize(MapUtils.checkAndCastMap(
                                map.get(KEY_OFF_HAND),
                                String.class, Object.class
                        )
                ): null,
                armorContents.toArray(new ItemStackBean[0])
        );
    }
}
