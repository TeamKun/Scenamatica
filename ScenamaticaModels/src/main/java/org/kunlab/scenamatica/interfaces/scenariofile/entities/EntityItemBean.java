package org.kunlab.scenamatica.interfaces.scenariofile.entities;

import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;

import java.util.UUID;

public interface EntityItemBean extends EntityBean
{
    // private static final String KEY_ITEM_STACK = "itemStack";    // トップレベルに ItemStackBean のキーを置くのでいらない。
    String KEY_PICKUP_DELAY = "pickupDelay";
    String KEY_OWNER = "owner";
    String KEY_THROWER = "thrower";
    String KEY_CAN_MOB_PICKUP = "canMobPickup";
    String KEY_WILL_AGE = "willAge";

    /**
     * アイテムのアイテムスタックです。
     */
    ItemStackBean getItemStack();

    /**
     * アイテムの拾い取り遅延時間です。
     */
    Integer getPickupDelay();

    /**
     * アイテムを拾ったプレイヤの UUID です。
     */
    UUID getOwner();

    /**
     * アイテムを投げたプレイヤの UUID です。
     */
    UUID getThrower();

    /**
     * アイテムをモブが拾えるかどうかです。
     */
    Boolean getCanMobPickup();

    /**
     * アイテムが時間経過で消滅するかどうかです。
     */
    Boolean getWillAge();
}
