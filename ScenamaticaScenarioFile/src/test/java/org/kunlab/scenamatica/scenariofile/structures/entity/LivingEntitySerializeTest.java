package org.kunlab.scenamatica.scenariofile.structures.entity;

import org.bukkit.inventory.EquipmentSlot;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.commons.specifiers.EntitySpecifierImpl;
import org.kunlab.scenamatica.commons.specifiers.PlayerSpecifierImpl;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.LivingEntityStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;
import org.kunlab.scenamatica.scenariofile.structures.inventory.ItemStackStructureSerializeTest;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LivingEntitySerializeTest
{
    public static final LivingEntityStructure FULFILLED = new LivingEntityStructureImpl(
            AEntityStructureSerializeTest.FULFILLED,
            114,
            514,
            1919,
            810,
            931,
            114.0,
            514,
            PlayerSpecifierImpl.tryDeserializePlayer("Me", StructureSerializerImpl.getInstance()),
            Collections.emptyList(),
            true,
            true,
            true,
            EntitySpecifierImpl.tryDeserialize("@e", StructureSerializerImpl.getInstance()),
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            114514,
            1919,
            ItemStackStructureSerializeTest.FULFILLED,
            114,
            514,
            true,
            EquipmentSlot.HAND,
            true,
            0.3f
    );/*
    String KEY_REMAINING_AIR = "remainAir";
    String KEY_MAX_AIR = "maxAir";
    String KEY_ARROW_COOLDOWN = "arrowCooldown";
    String KEY_ARROWS_IN_BODY = "arrowsInBody";
    String KEY_MAX_NO_DAMAGE_TICKS = "maxNoDamageTicks";
    String KEY_LAST_DAMAGE = "lastDamage";
    String KEY_NO_DAMAGE_TICKS = "noDamageTicks";
    String KEY_KILLER = "killer";
    String KEY_POTION_EFFECTS = "potions";
    String KEY_POTION_EFFECTS_AMBIENT = "ambient";
    String KEY_POTION_EFFECTS_AMPLIFIER = "amplifier";
    String KEY_POTION_EFFECTS_DURATION = "duration";
    String KEY_POTION_EFFECTS_TYPE = "type";
    String KEY_POTION_EFFECTS_SHOW_PARTICLES = "particle";
    String KEY_POTION_EFFECTS_SHOW_ICON = "icon";
    String KEY_REMOVE_WHEN_FAR_AWAY = "removeWhenFarAway";
    String KEY_CAN_PICKUP_ITEMS = "canPickupItems";
    String KEY_LEASHED = "leashed";
    String KEY_LEASH_HOLDER = "leashHolder";
    String KEY_GLIDING = "gliding";
    String KEY_SWIMMING = "swimming";
    String KEY_RIPTIDING = "riptiding";
    String KEY_SLEEPING = "sleeping";
    String KEY_AI = "ai";
    String KEY_COLLIDABLE = "collidable";
    String KEY_INVISIBLE = "invisible";
    // Paper
    String KEY_ARROWS_STUCK = "arrowsStuck";
    String KEY_SHIELD_BLOCKING_DELAY = "shieldBlockingDelay";
    String KEY_ACTIVE_ITEM = "activeItem";
    String KEY_ITEM_USE_REMAIN_TIME = "itemUseRemainTime";
    String KEY_HAND_RAISED_TIME = "handRaisedTime";
    String KEY_IS_HAND_RAISED = "isHandRaised";
    String KEY_HAND_RAISED = "handRaised";
    String KEY_JUMPING = "jumping";
    String KEY_HURT_DIRECTION = "hurtDirection";*/

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(AEntityStructureSerializeTest.FULFILLED_MAP)
    {{
        this.put("remainAir", 114);
        this.put("maxAir", 514);
        this.put("arrowCooldown", 1919);
        this.put("arrowsInBody", 810);
        this.put("maxNoDamageTicks", 931);
        this.put("lastDamage", 114.0);
        this.put("noDamageTicks", 514);
        this.put("killer", "Me");
        /*
        this.put("potions", Collections.singletonList(new HashMap<String, Object>()
        {{
            this.put("type", "BAD_OMEN");
            this.put("duration", 100);
            this.put("amplifier", 1);
            this.put("ambient", false);
            this.put("particle", false);
            this.put("icon", false);
        }}));*/
        // ↑は, Bukkit が必要なので単体テストできない。
        this.put("removeWhenFarAway", true);
        this.put("canPickupItems", true);
        this.put("leashed", true);
        this.put("leashHolder", "@e");
        this.put("gliding", true);
        this.put("swimming", true);
        this.put("riptiding", true);
        this.put("sleeping", true);
        this.put("ai", true);
        this.put("collidable", true);
        this.put("invisible", true);
        // Paper
        this.put("arrowsStuck", 114514);
        this.put("shieldBlockingDelay", 1919);
        this.put("activeItem", ItemStackStructureSerializeTest.FULFILLED_MAP);
        this.put("itemUseRemainTime", 114);
        this.put("handRaisedTime", 514);
        this.put("isHandRaised", true);
        this.put("handRaised", EquipmentSlot.HAND.name());
        this.put("jumping", true);
        this.put("hurtDirection", 0.3f);
    }};

    public static final LivingEntityStructure EMPTY = new LivingEntityStructureImpl();

    public static final Map<String, Object> EMPTY_MAP = new HashMap<>(AEntityStructureSerializeTest.EMPTY_MAP);

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = LivingEntityStructureImpl.serializeLivingEntity(FULFILLED, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        EntityStructure entity = LivingEntityStructureImpl.deserializeLivingEntity(FULFILLED_MAP, StructureSerializerImpl.getInstance());

        assertEquals(FULFILLED, entity);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = LivingEntityStructureImpl.serializeLivingEntity(EMPTY, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        EntityStructure entity = LivingEntityStructureImpl.deserializeLivingEntity(EMPTY_MAP, StructureSerializerImpl.getInstance());

        assertEquals(EMPTY, entity);
    }
}
