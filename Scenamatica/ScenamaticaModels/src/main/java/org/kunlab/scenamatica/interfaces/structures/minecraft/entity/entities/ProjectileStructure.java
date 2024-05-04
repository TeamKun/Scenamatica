package org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities;

import org.bukkit.entity.Projectile;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.ProjectileSourceStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;

/**
 * 投射物のインターフェースです。
 */
public interface ProjectileStructure extends EntityStructure, Mapped<Projectile>
{
    String KEY_SHOOTER = "shooter";
    String KEY_DOES_BOUNCE = "bounce";

    /**
     * この投射物を撃ったエンティティを取得します。
     *
     * @return 撃ったエンティティ
     */
    ProjectileSourceStructure getShooter();

    /**
     * この投射物が跳ね返るかどうかを取得します。
     *
     * @return 跳ね返るかどうか
     */
    Boolean getDoesBounce();
}
