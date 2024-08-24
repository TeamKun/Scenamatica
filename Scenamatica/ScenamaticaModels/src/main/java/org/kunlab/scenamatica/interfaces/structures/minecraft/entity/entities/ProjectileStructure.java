package org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities;

import org.bukkit.entity.Projectile;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.ProjectileSourceStructure;

/**
 * 投射物のインターフェースです。
 */
@TypeDoc(
        name = "Projectile",
        description = "投射物の情報を格納します。",
        mappingOf = Projectile.class,
        properties = {
                @TypeProperty(
                        name = ProjectileStructure.KEY_SHOOTER,
                        description = "投射物を撃ったエンティティです。",
                        type = ProjectileSourceStructure.class
                ),
                @TypeProperty(
                        name = ProjectileStructure.KEY_DOES_BOUNCE,
                        description = "投射物が跳ね返るかどうかです。",
                        type = boolean.class
                )
        }

)
public interface ProjectileStructure extends EntityStructure
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

    @Override
    default boolean canApplyTo(@Nullable Object target)
    {
        return target instanceof Projectile;
    }
}
