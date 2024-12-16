package org.kunlab.scenamatica.nms.types.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LightningStrike;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.nms.NMSWrapped;
import org.kunlab.scenamatica.nms.Versioned;
import org.kunlab.scenamatica.nms.enums.entity.NMSLightningStrikeCause;

/**
 * WorldServer の NMS による実装を提供します。
 */
public interface NMSWorldServer extends NMSWrapped
{
    @Override
    World getBukkit();

    /**
     * ワールドデータを取得します。
     *
     * @return ワールドデータ
     */
    @NotNull
    NMSWorldData getWorldData();

    /**
     * チャンクプロバイダを取得します。
     *
     * @return チャンクプロバイダ
     */
    @NotNull
    NMSChunkProvider getChunkProvider();

    /**
     * エンティティマネージャを取得します。
     *
     * @return エンティティマネージャ
     */
    @NotNull
    @Versioned(from = "1.17")
    NMSPersistentEntitySectionManager<Entity> getEntityManager();

    /**
     * ワールドに落雷を発生させます。
     *
     * @param location 落雷の位置
     * @param isEffect 落雷のエフェクトを表示するかどうか
     * @param cause    落雷の原因
     */
    @Versioned(from = "1.14")
    void strikeLightning(@NotNull Location location, boolean isEffect, @NotNull NMSLightningStrikeCause cause);

    /**
     * ワールドに落雷を発生させます。
     *
     * @param strike 落雷のエンティティ
     * @param cause  落雷の原因
     */
    @Versioned(from = "1.14")
    void strikeLightning(@NotNull LightningStrike strike, @NotNull NMSLightningStrikeCause cause);
}
