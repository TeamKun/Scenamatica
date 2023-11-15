package org.kunlab.scenamatica.interfaces.scenariofile.entity;

import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.Bean;

/**
 * ダメージを表すインターフェースです。
 */
public interface DamageBean extends Bean
{
    String KEY_MODIFIER = "modifier";
    String KEY_CAUSE = "cause";
    String KEY_DAMAGE = "damage";

    /**
     * ダメージの修飾子です。
     *
     * @return ダメージの修飾子
     */
    @NotNull
    @SuppressWarnings("deprecation")
    EntityDamageEvent.DamageModifier getModifier();

    /**
     * ダメージの原因です。
     *
     * @return ダメージの原因
     */
    @NotNull
    EntityDamageEvent.DamageCause getCause();

    /**
     * ダメージの量です。
     *
     * @return ダメージの量
     */
    double getDamage();
}
