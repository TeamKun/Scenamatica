package net.kunmc.lab.scenamatica.scenario.beans.inventory;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * インベントリの定義を表すクラスです。
 */
@Value
public class InventoryBean implements Serializable
{
    private static final String KEY_SIZE = "size";
    private static final String KEY_TITLE = "title";
    private static final String KEY_MAIN_CONTENTS = "items";

    /**
     * インベントリのサイズです。
     */
    int size;
    /**
     * インベントリのタイトルです。
     */
    @Nullable
    String title;
    /**
     * インベントリのアイテムです。
     */
    @NotNull
    Map<Integer, ItemStackBean> mainContents;

    /**
     * インベントリの情報をMapにシリアライズします。
     *
     * @param bean インベントリの情報
     * @return シリアライズされたMap
     */
    public static Map<String, Object> serialize(InventoryBean bean)
    {
        Map<Integer, Object> contents = new HashMap<>();
        for (Map.Entry<Integer, ItemStackBean> entry : bean.mainContents.entrySet())
            contents.put(entry.getKey(), ItemStackBean.serialize(entry.getValue()));

        Map<String, Object> map = new HashMap<>();
        map.put(KEY_SIZE, bean.size);
        MapUtils.putIfNotNull(map, KEY_TITLE, bean.title);
        MapUtils.putMapIfNotEmpty(map, KEY_MAIN_CONTENTS, contents);
        return map;
    }

    /**
     * Mapがインベントリの情報を表すMapかどうかを検証します。
     *
     * @param map 検証するMap
     * @throws IllegalArgumentException 必須項目が含まれていない場合か, 型が不正な場合
     */
    public static void validateMap(Map<String, Object> map)
    {
        MapUtils.checkType(map, KEY_SIZE, Integer.class);
        MapUtils.checkTypeIfContains(map, KEY_TITLE, String.class);

        Map<Integer, Object> contents = MapUtils.checkAndCastMap(
                map.get(KEY_MAIN_CONTENTS),
                Integer.class,
                Object.class
        );

        for (Map.Entry<Integer, Object> entry : contents.entrySet())
            ItemStackBean.validateMap(MapUtils.checkAndCastMap(
                    entry.getValue(),
                    String.class,
                    Object.class
            ));
    }

    /**
     * Mapからインベントリの情報をデシリアライズします。
     *
     * @param map シリアライズされたMap
     * @return インベントリの情報
     */
    public static InventoryBean deserialize(Map<String, Object> map)
    {
        validateMap(map);

        Map<Integer, Object> contents = MapUtils.checkAndCastMap(
                map.get(KEY_MAIN_CONTENTS),
                Integer.class,
                Object.class

        );
        Map<Integer, ItemStackBean> mainContents = new HashMap<>();
        for (Map.Entry<Integer, Object> entry : contents.entrySet())
            mainContents.put(
                    entry.getKey(),
                    ItemStackBean.deserialize(MapUtils.checkAndCastMap(
                            entry.getValue(),
                            String.class,
                            Object.class
                    ))
            );

        return new InventoryBean(
                (int) map.get(KEY_SIZE),
                MapUtils.getOrNull(map, KEY_TITLE),
                mainContents
        );
    }
}
