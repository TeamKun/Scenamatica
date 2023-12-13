package org.kunlab.scenamatica.scenariofile.structures.entity;

import lombok.SneakyThrows;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.AEntityStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.entities.AEntityStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AEntityStructureSerializeTest
{
    public static final AEntityStructure FULFILLED = new AEntityStructureImpl(
            EntityType.UNKNOWN,
            new Location(null, 1145, 1419, 19, 8, 10),
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

    public static final AEntityStructure EMPTY = new AEntityStructureImpl();

    public static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    @BeforeAll
    @SuppressWarnings("unchecked")
    @SneakyThrows({NoSuchFieldException.class, IllegalAccessException.class})
    static void initPotionType()
    {
        Map<String, PotionEffectType> byName;
        PotionEffectType[] byID;

        Field fByName = PotionEffectType.class.getDeclaredField("byName");
        Field fByID = PotionEffectType.class.getDeclaredField("byId");
        fByName.setAccessible(true);
        fByID.setAccessible(true);

        byName = (Map<String, PotionEffectType>) fByName.get(null);
        byID = (PotionEffectType[]) fByID.get(null);

        byName.put("bad_omen", PotionEffectType.BAD_OMEN);
        byID[31] = PotionEffectType.BAD_OMEN;
    }

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = AEntityStructureImpl.serialize((EntityStructure) FULFILLED, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        EntityStructure entity = AEntityStructureImpl.deserialize(FULFILLED_MAP, StructureSerializerImpl.getInstance());

        assertEquals(FULFILLED, entity);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = AEntityStructureImpl.serialize((EntityStructure) EMPTY, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        EntityStructure entity = AEntityStructureImpl.deserialize(EMPTY_MAP, StructureSerializerImpl.getInstance());

        assertEquals(EMPTY, entity);
    }
}
