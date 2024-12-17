package org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities;

import org.bukkit.entity.LightningStrike;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;

/**
 * 雷を表すインターフェースです。
 */
@TypeDoc(
        name = "LightningStrike",
        description = "雷の情報を格納します。",
        mappingOf = LightningStrike.class,
        properties = {
                @TypeProperty(
                        name = LightningStrikeStructure.KEY_EFFECT,
                        description = "雷がダメージを与えるかどうかです。",
                        type = boolean.class
                )
        }
)
public interface LightningStrikeStructure extends WeatherStructure
{
    String KEY_EFFECT = "effect";

    /**
     * 雷がダメージを与えるかどうかを取得します。
     *
     * @return ダメージを与えるかどうか
     */
    Boolean isEffect();
}
