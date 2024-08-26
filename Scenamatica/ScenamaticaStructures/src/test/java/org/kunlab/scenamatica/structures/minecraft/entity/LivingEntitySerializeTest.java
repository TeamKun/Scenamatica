package org.kunlab.scenamatica.structures.minecraft.entity;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.LivingEntityStructure;
import org.kunlab.scenamatica.structures.minecraft.StructureSerializerMock;
import org.kunlab.scenamatica.structures.minecraft.inventory.ItemStackStructureSerializeTest;
import org.kunlab.scenamatica.structures.minecraft.utils.MapTestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class LivingEntitySerializeTest
{
    public static final LivingEntityStructure FULFILLED = new LivingEntityStructureImpl(
            EntityStructureSerializeTest.FULFILLED,
            0.5,
            114,
            514,
            1919,
            810,
            931,
            114.0,
            514,
            StructureSerializerMock.getInstance().tryDeserializePlayerSpecifier("@a"),
            Collections.emptyList(),
            true,
            true,
            true,
            StructureSerializerMock.getInstance().tryDeserializeEntitySpecifier("@e"),
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
            true
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(EntityStructureSerializeTest.FULFILLED_MAP)
    {{
        this.put("eyeHeight", 0.5d);
        this.put("remainAir", 114);
        this.put("maxAir", 514);
        this.put("arrowCooldown", 1919);
        this.put("arrowsInBody", 810);
        this.put("maxNoDamageTicks", 931);
        this.put("lastDamage", 114.0);
        this.put("noDamageTicks", 514);
        this.put("killer", "@a");
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
    }};

    public static final LivingEntityStructure EMPTY = new LivingEntityStructureImpl();

    public static final Map<String, Object> EMPTY_MAP = new HashMap<>(EntityStructureSerializeTest.EMPTY_MAP);

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = LivingEntityStructureImpl.serializeLivingEntity(FULFILLED, StructureSerializerMock.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        EntityStructure entity = LivingEntityStructureImpl.deserializeLivingEntity(FULFILLED_MAP, StructureSerializerMock.getInstance());

        assertEquals(FULFILLED, entity);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = LivingEntityStructureImpl.serializeLivingEntity(EMPTY, StructureSerializerMock.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        EntityStructure entity = LivingEntityStructureImpl.deserializeLivingEntity(EMPTY_MAP, StructureSerializerMock.getInstance());

        assertEquals(EMPTY, entity);
    }
}
