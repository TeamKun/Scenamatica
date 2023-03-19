package net.kunmc.lab.scenamatica.scenariofile.interfaces.entities;

import org.bukkit.event.entity.EntityDamageEvent;

/**
 * ダメージを表すインターフェースです。
 */
public interface DamageBean
{
    String KEY_MODIFIER = "modifier";
    String KEY_CAUSE = "cause";
    String KEY_DAMAGE = "damage";

    /**
     * ダメージの修飾子です。
     *
     * @return ダメージの修飾子
     */
    @SuppressWarnings("deprecation")
    EntityDamageEvent.DamageModifier getModifier();

    /**
     * ダメージの原因です。
     *
     * @return ダメージの原因
     */
    EntityDamageEvent.DamageCause getCause();

    /**
     * ダメージの量です。
     *
     * @return ダメージの量
     */
    double getDamage();
}
