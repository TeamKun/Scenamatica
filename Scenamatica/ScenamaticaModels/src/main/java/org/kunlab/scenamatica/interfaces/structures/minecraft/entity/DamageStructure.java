package org.kunlab.scenamatica.interfaces.structures.minecraft.entity;

import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.annotations.DynamicTypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;

import java.util.Map;

/**
 * ダメージを表すインターフェースです。
 */
@TypeDoc(
        name = "Damage",
        description = "ダメージの情報を格納します。",
        mappingOf = EntityDamageEvent.class,
        properties = {
                @TypeProperty(
                        name = DamageStructure.KEY_MODIFIER,
                        description = "ダメージの修飾子です。",
                        type = Map.class
                ),
                @TypeProperty(
                        name = DamageStructure.KEY_CAUSE,
                        description = "ダメージの原因です。",
                        type = EntityDamageEvent.DamageCause.class
                ),
                @TypeProperty(
                        name = DamageStructure.KEY_DAMAGE,
                        description = "ダメージの量です。",
                        type = double.class
                )
        }
)
@Category("entity")
public interface DamageStructure extends Structure, Mapped<EntityDamageEvent>
{
    String KEY_MODIFIER = "modifiers";
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
