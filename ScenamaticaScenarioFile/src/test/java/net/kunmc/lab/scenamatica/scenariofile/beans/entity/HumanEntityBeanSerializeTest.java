package net.kunmc.lab.scenamatica.scenariofile.beans.entity;

import net.kunmc.lab.scenamatica.scenariofile.beans.entities.HumanEntityBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.inventory.InventoryBeanSerializeTest;
import net.kunmc.lab.scenamatica.scenariofile.beans.inventory.PlayerInventoryBeanSerializeTest;
import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.bukkit.GameMode;
import org.bukkit.inventory.MainHand;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HumanEntityBeanSerializeTest
{
    public static final HumanEntityBean FULFILLED = new HumanEntityBean(
            EntityBeanSerializeTest.FULFILLED,
            PlayerInventoryBeanSerializeTest.FULFILLED,
            InventoryBeanSerializeTest.FULFILLED,
            MainHand.LEFT,
            GameMode.ADVENTURE,
            20
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(EntityBeanSerializeTest.FULFILLED_MAP)
    {{
        put("inventory", PlayerInventoryBeanSerializeTest.FULFILLED_MAP);
        put("enderChest", InventoryBeanSerializeTest.FULFILLED_MAP);
        put("mainHand", "LEFT");
        put("gamemode", "ADVENTURE");
        put("food", 20);
    }};

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = HumanEntityBean.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        HumanEntityBean bean = HumanEntityBean.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, bean);
    }
}
