package org.kunlab.scenamatica.interfaces.scenariofile.entity.entities;

import org.bukkit.entity.Projectile;
import org.kunlab.scenamatica.interfaces.scenariofile.Creatable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;

/**
 * 投射物のインターフェースです。
 */
public interface ProjectileStructure extends EntityStructure, Mapped<Projectile>, Creatable<Projectile>
{
    String KEY_SHOOTER = "shooter";
    String KEY_DOES_BOUNCE = "bounce";

    /**
     * この投射物を撃ったエンティティを取得します。
     *
     * @return 撃ったエンティティ
     */
    EntityStructure getShooter();

    /**
     * この投射物が跳ね返るかどうかを取得します。
     *
     * @return 跳ね返るかどうか
     */
    Boolean getDoesBounce();
}
