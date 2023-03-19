package net.kunmc.lab.scenamatica.scenariofile.beans.entities;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * ダメージを表すクラスです。
 */
@Value
@SuppressWarnings("deprecation")
public class DamageBean
{
    public static final String KEY_MODIFIER = "modifier";
    public static final String KEY_CAUSE = "cause";
    public static final String KEY_DAMAGE = "damage";

    /**
     * ダメージ編集のタイプです。
     */
    @NotNull
    EntityDamageEvent.DamageModifier modifier;

    /**
     * ダメージのケースです。
     */
    @NotNull
    EntityDamageEvent.DamageCause cause;

    /**
     * ダメージの量です。
     */
    double damage;

    /**
     * ダメージの情報をMapにシリアライズします。
     *
     * @param bean ダメージの情報
     * @return シリアライズされたMap
     */
    public static Map<String, Object> serialize(@NotNull DamageBean bean)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_DAMAGE, bean.damage);
        if (bean.modifier != EntityDamageEvent.DamageModifier.BASE)
            map.put(KEY_MODIFIER, bean.modifier.name());
        if (bean.cause != EntityDamageEvent.DamageCause.CUSTOM)
            map.put(KEY_CAUSE, bean.cause.name());

        return map;
    }

    /**
     * Mapがシリアライズされたダメージの情報を表すMapかどうかを検証します。
     *
     * @param map 判定するMap
     * @return シリアライズされたダメージの情報かどうか
     * @throws IllegalArgumentException 必須のキーが存在しない場合
     */
    public static boolean validate(@NotNull Map<String, Object> map)
    {
        MapUtils.checkEnumNameIfContains(map, KEY_MODIFIER, EntityDamageEvent.DamageModifier.class);
        MapUtils.checkEnumNameIfContains(map, KEY_CAUSE, EntityDamageEvent.DamageCause.class);
        MapUtils.checkType(map, KEY_DAMAGE, Number.class);

        return true;
    }

    /**
     * シリアライズされたMapからダメージの情報を復元します。
     *
     * @param map シリアライズされたMap
     * @return ダメージの情報
     * @throws IllegalArgumentException 必須のキーが存在しない場合
     */
    public static DamageBean deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);
        return new DamageBean(
                MapUtils.getAsEnumOrDefault(map, KEY_MODIFIER, EntityDamageEvent.DamageModifier.class, EntityDamageEvent.DamageModifier.BASE),
                MapUtils.getAsEnumOrDefault(map, KEY_CAUSE, EntityDamageEvent.DamageCause.class, EntityDamageEvent.DamageCause.CUSTOM),
                Double.parseDouble(map.get(KEY_DAMAGE).toString())
        );
    }
}
