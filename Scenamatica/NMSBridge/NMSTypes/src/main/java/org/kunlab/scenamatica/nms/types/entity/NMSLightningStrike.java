package org.kunlab.scenamatica.nms.types.entity;

import org.bukkit.entity.LightningStrike;

/**
 * {@link LightningStrike} のラッパです。
 */
public interface NMSLightningStrike extends NMSEntity
{
    /**
     * 雷が視覚的なものだけかどうかを取得します。
     *
     * @param visualOnly 視覚的なものだけかどうか
     * @see LightningStrike#isEffect()
     */
    void setVisualOnly(boolean visualOnly);

    /**
     * ラップしている {@link LightningStrike} を取得します。
     *
     * @return {@link LightningStrike}
     */
    @Override
    LightningStrike getBukkit();
}
