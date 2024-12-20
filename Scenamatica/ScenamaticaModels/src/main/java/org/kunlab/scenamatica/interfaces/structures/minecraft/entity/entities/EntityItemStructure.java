package org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities;

import org.bukkit.entity.Item;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;

@TypeDoc(
        name = "EntityItem",
        description = "アイテムエンティティの情報を格納します。",
        mappingOf = Item.class,
        properties = {
                @TypeProperty(
                        name = EntityItemStructure.KEY_PICKUP_DELAY,
                        description = "アイテムの拾い取りにかかる時間です。",
                        type = int.class
                ),
                @TypeProperty(
                        name = EntityItemStructure.KEY_OWNER,
                        description = "アイテムを拾ったエンティティです。",
                        type = EntitySpecifier.class
                ),
                @TypeProperty(
                        name = EntityItemStructure.KEY_THROWER,
                        description = "アイテムを投げたエンティティです。",
                        type = EntitySpecifier.class
                ),
                @TypeProperty(
                        name = EntityItemStructure.KEY_CAN_MOB_PICKUP,
                        description = "アイテムをモブが拾えるかどうかです。",
                        type = boolean.class
                ),
                @TypeProperty(
                        name = EntityItemStructure.KEY_WILL_AGE,
                        description = "アイテムが時間経過で消滅するかどうかです。",
                        type = boolean.class
                )
        }

)
public interface EntityItemStructure extends EntityStructure
{
    // public static final String KEY_ITEM_STACK = "itemStack";    // トップレベルに ItemStackStructure のキーを置くのでいらない。
    String KEY_PICKUP_DELAY = "pickupDelay";
    String KEY_OWNER = "owner";
    String KEY_THROWER = "thrower";
    String KEY_CAN_MOB_PICKUP = "canMobPickup";
    String KEY_WILL_AGE = "willAge";

    /**
     * アイテムのアイテムスタックです。
     */
    ItemStackStructure getItemStack();

    /**
     * アイテムの拾い取り遅延時間です。
     */
    Integer getPickupDelay();

    /**
     * アイテムを拾ったエンティティです。
     */
    EntitySpecifier<?> getOwner();

    /**
     * アイテムを投げたエンティティです。
     */
    EntitySpecifier<?> getThrower();

    /**
     * アイテムをモブが拾えるかどうかです。
     */
    Boolean getCanMobPickup();

    /**
     * アイテムが時間経過で消滅するかどうかです。
     */
    Boolean getWillAge();

    @Override
    default boolean canApplyTo(@Nullable Object target)
    {
        return target instanceof Item;
    }
}
