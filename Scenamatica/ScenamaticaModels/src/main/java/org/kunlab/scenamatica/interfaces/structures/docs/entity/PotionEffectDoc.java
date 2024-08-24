package org.kunlab.scenamatica.interfaces.structures.docs.entity;

import org.bukkit.Material;
import org.kunlab.scenamatica.bookkeeper.annotations.Category;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;

@TypeDoc(
        name = "PotionEffect",
        description = "ポーション効果の情報を格納します。",
        mappingOf = org.bukkit.potion.PotionEffect.class,
        properties = {
                @TypeProperty(
                        name = "type",
                        description = "ポーション効果の種類です。",
                        type = PotionEffectDoc.EnumPotionEffect.class
                ),
                @TypeProperty(
                        name = "duration",
                        description = "ポーション効果の持続時間です。",
                        type = int.class
                ),
                @TypeProperty(
                        name = "amplifier",
                        description = "ポーション効果の強度です。",
                        type = int.class
                ),
                @TypeProperty(
                        name = "ambient",
                        description = "ポーション効果がアンビエントかどうかです。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = "particles",
                        description = "ポーション効果がパーティクルを表示するかどうかです。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = "icon",
                        description = "ポーション効果のアイコンです。",
                        type = Material.class
                )
        }
)
@Category(inherit = EntityStructure.class)
public interface PotionEffectDoc
{
    enum EnumPotionEffect
    {
        SPEED,
        SLOW,
        FAST_DIGGING,
        SLOW_DIGGING,
        INCREASE_DAMAGE,
        HEAL,
        HARM,
        JUMP,
        CONFUSION,
        REGENERATION,
        DAMAGE_RESISTANCE,
        FIRE_RESISTANCE,
        WATER_BREATHING,
        INVISIBILITY,
        BLINDNESS,
        NIGHT_VISION,
        HUNGER,
        WEAKNESS,
        POISON,
        WITHER,
        HEALTH_BOOST,
        ABSORPTION,
        SATURATION,
        GLOWING,
        LEVITATION,
        LUCK,
        UNLUCK,
        SLOW_FALLING,
        CONDUIT_POWER,
        DOLPHINS_GRACE,
        BAD_OMEN,
        HERO_OF_THE_VILLAGE
    }
}
