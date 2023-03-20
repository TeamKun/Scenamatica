package net.kunmc.lab.scenamatica.scenariofile.beans.entity;

import lombok.SneakyThrows;
import net.kunmc.lab.scenamatica.scenariofile.beans.entities.EntityBeanImpl;
import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.entities.EntityBean;
import org.bukkit.Location;
import org.bukkit.potion.PotionEffectType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EntityBeanSerializeTest
{
    public static final EntityBean FULFILLED = new EntityBeanImpl(
            new Location(null, 1145, 1419, 19, 8, 10),
            "YajuSNPI",
            UUID.fromString("a1b1c4d5-e1f4-a1b9-c1d9-e8f1a0bcdef1"),
            true,
            false,
            Arrays.asList("tagTest", "tagTest2"),
            22,
            20,
            DamageBeanSerializeTest.FULFILLED,
            Collections.emptyList()
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
            )
             */
            // ↑は, Bukkit が必要なので単体テストできない。
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("loc", new HashMap<String, Object>()
        {{
            this.put("x", 1145.0);
            this.put("y", 1419.0);
            this.put("z", 19.0);
            this.put("yaw", 8.0f);
            this.put("pitch", 10.0f);
        }});
        this.put("customName", "YajuSNPI");

        this.put("uuid", "a1b1c4d5-e1f4-a1b9-c1d9-e8f1a0bcdef1");
        this.put("glowing", true);
        this.put("gravity", false);
        this.put("tags", Arrays.asList("tagTest", "tagTest2"));
        this.put("maxHealth", 22);
        this.put("health", 20);
        this.put("lastDamage", DamageBeanSerializeTest.FULFILLED_MAP);/*
        this.put("potion", Collections.singletonList(new HashMap<String, Object>()
        {{
            this.put("type", "BAD_OMEN");
            this.put("duration", 100);
            this.put("amplifier", 1);
            this.put("ambient", false);
            this.put("particle", false);
            this.put("icon", false);
        }}));*/
        // ↑は, Bukkit が必要なので単体テストできない。
    }};

    public static final EntityBean EMPTY = new EntityBeanImpl();

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
        Map<String, Object> map = EntityBeanImpl.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        EntityBean entity = EntityBeanImpl.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, entity);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = EntityBeanImpl.serialize(EMPTY);

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        EntityBean entity = EntityBeanImpl.deserialize(EMPTY_MAP);

        assertEquals(EMPTY, entity);
    }
}
