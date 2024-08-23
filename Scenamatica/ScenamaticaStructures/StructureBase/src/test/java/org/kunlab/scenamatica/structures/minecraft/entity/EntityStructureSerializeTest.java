package org.kunlab.scenamatica.structures.minecraft.entity;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.structures.minecraft.StructureSerializerMock;
import org.kunlab.scenamatica.structures.minecraft.misc.LocationStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.utils.MapTestUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityStructureSerializeTest
{
    public static final EntityStructure FULFILLED = new EntityStructureImpl(
            EntityType.UNKNOWN,
            LocationStructureImpl.of(new Location(null, 1145, 1419, 19, 8, 10)),
            new Vector(11, 45, 14),
            "YajuSNPI",
            UUID.fromString("a1b1c4d5-e1f4-a1b9-c1d9-e8f1a0bcdef1"),
            true,
            false,
            true,
            true,
            true,
            Arrays.asList("tagTest", "tagTest2"),
            22,
            20,
            DamageStructureSerializeTest.FULFILLED,
            /*
            Collections.singletonList(
                    new PotionEffect(
                            PotionEffectType.BAD_OMEN,
                            100,
                            1,
                            false,
                            false,
                            false
                    )
            ),
             */
            // ↑は, Bukkit が必要なので単体テストできない。
            1,
            1,
            4,
            true,
            81.0f
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("type", "UNKNOWN");
        this.put("loc", new HashMap<String, Object>()
        {{
            this.put("x", 1145.0);
            this.put("y", 1419.0);
            this.put("z", 19.0);
            this.put("yaw", 8.0f);
            this.put("pitch", 10.0f);
        }});
        this.put("velocity", new HashMap<String, Object>()
        {{
            this.put("x", 11.0);
            this.put("y", 45.0);
            this.put("z", 14.0);
        }});
        this.put("customName", "YajuSNPI");

        this.put("uuid", "a1b1c4d5-e1f4-a1b9-c1d9-e8f1a0bcdef1");
        this.put("glowing", true);
        this.put("gravity", false);
        this.put("silent", true);
        this.put("customNameVisible", true);

        this.put("invulnerable", true);
        this.put("tags", Arrays.asList("tagTest", "tagTest2"));
        this.put("maxHealth", 22);
        this.put("health", 20);
        this.put("lastDamageCause", DamageStructureSerializeTest.FULFILLED_MAP);/*
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
        this.put("fireTicks", 1);
        this.put("ticksLived", 1);
        this.put("portalCooldown", 4);
        this.put("persistent", true);
        this.put("fallDistance", 81.0f);
    }};

    public static final EntityStructure EMPTY = new EntityStructureImpl();

    public static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = EntityStructureImpl.serialize((EntityStructure) FULFILLED, StructureSerializerMock.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        EntityStructure entity = EntityStructureImpl.deserialize(FULFILLED_MAP, StructureSerializerMock.getInstance());

        assertEquals(FULFILLED, entity);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = EntityStructureImpl.serialize((EntityStructure) EMPTY, StructureSerializerMock.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        EntityStructure entity = EntityStructureImpl.deserialize(EMPTY_MAP, StructureSerializerMock.getInstance());

        assertEquals(EMPTY, entity);
    }
}
