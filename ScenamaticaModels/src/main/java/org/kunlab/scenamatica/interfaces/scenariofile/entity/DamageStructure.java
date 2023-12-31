package org.kunlab.scenamatica.interfaces.scenariofile.entity;

import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

import java.util.Map;

/**
 * ダメージを表すインターフェースです。
 */
public interface DamageStructure extends Structure
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
    Map<EntityDamageEvent.DamageModifier, Double> getModifiers();

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
