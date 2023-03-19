package net.kunmc.lab.scenamatica.scenariofile.beans.entity;

import net.kunmc.lab.scenamatica.scenariofile.beans.entities.DamageBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.bukkit.event.entity.EntityDamageEvent;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("deprecation")
public class DamageBeanSerializeTest
{
    public static final DamageBean FULFILLED = new DamageBean(
            EntityDamageEvent.DamageModifier.BLOCKING,
            EntityDamageEvent
                    .DamageCause.CRAMMING,
            1.0
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("damage", 1.0);
        this.put("modifier", "BLOCKING");
        this.put("cause", "CRAMMING");
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = DamageBean.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        DamageBean bean = DamageBean.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, bean);
    }
}
