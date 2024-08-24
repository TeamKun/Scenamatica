package org.kunlab.scenamatica.structures.minecraft.entity;

import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.DamageStructure;
import org.kunlab.scenamatica.structures.minecraft.utils.MapTestUtil;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("deprecation")
public class DamageStructureSerializeTest
{
    public static final DamageStructure FULFILLED = new DamageStructureImpl(
            new EnumMap<EntityDamageEvent.DamageModifier, Double>(EntityDamageEvent.DamageModifier.class)
            {{
                this.put(EntityDamageEvent.DamageModifier.ABSORPTION, 11.0);
                this.put(EntityDamageEvent.DamageModifier.ARMOR, 15.0);
                this.put(EntityDamageEvent.DamageModifier.BASE, 10.0);
                this.put(EntityDamageEvent.DamageModifier.BLOCKING, 12.0);
                this.put(EntityDamageEvent.DamageModifier.HARD_HAT, 16.0);
                this.put(EntityDamageEvent.DamageModifier.MAGIC, 13.0);
                this.put(EntityDamageEvent.DamageModifier.RESISTANCE, 14.0);

            }},
            EntityDamageEvent
                    .DamageCause.CRAMMING,
            1.0
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("damage", 1.0);
        this.put("cause", "CRAMMING");
        this.put("modifiers", new HashMap<String, Object>()
        {{
            this.put("absorption", 11.0);
            this.put("armor", 15.0);
            this.put("base", 10.0);
            this.put("blocking", 12.0);
            this.put("hard_hat", 16.0);
            this.put("magic", 13.0);
            this.put("resistance", 14.0);
        }});
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = DamageStructureImpl.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        DamageStructure structure = DamageStructureImpl.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, structure);
    }
}
