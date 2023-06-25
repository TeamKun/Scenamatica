package org.kunlab.scenamatica.scenariofile.beans.entity;

import org.bukkit.GameMode;
import org.bukkit.inventory.MainHand;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.entities.HumanEntityBean;
import org.kunlab.scenamatica.scenariofile.BeanSerializerImpl;
import org.kunlab.scenamatica.scenariofile.beans.entities.HumanEntityBeanImpl;
import org.kunlab.scenamatica.scenariofile.beans.inventory.InventoryBeanSerializeTest;
import org.kunlab.scenamatica.scenariofile.beans.inventory.PlayerInventoryBeanSerializeTest;
import org.kunlab.scenamatica.scenariofile.beans.utils.MapTestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HumanEntityBeanSerializeTest
{
    public static final HumanEntityBean FULFILLED = new HumanEntityBeanImpl(
            EntityBeanSerializeTest.FULFILLED,
            PlayerInventoryBeanSerializeTest.FULFILLED,
            InventoryBeanSerializeTest.FULFILLED,
            MainHand.LEFT,
            GameMode.ADVENTURE,
            20
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(EntityBeanSerializeTest.FULFILLED_MAP)
    {{
        this.put("inventory", PlayerInventoryBeanSerializeTest.FULFILLED_MAP);
        this.put("enderChest", InventoryBeanSerializeTest.FULFILLED_MAP);
        this.put("mainHand", "LEFT");
        this.put("gamemode", "ADVENTURE");
        this.put("food", 20);
    }};

    public static final HumanEntityBean EMPTY = new HumanEntityBeanImpl(
            EntityBeanSerializeTest.EMPTY,
            null,
            null,
            MainHand.RIGHT,
            GameMode.SURVIVAL,
            null
    );

    public static final Map<String, Object> EMPTY_MAP =
            new HashMap<>(EntityBeanSerializeTest.EMPTY_MAP);

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = HumanEntityBeanImpl.serialize(FULFILLED, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        HumanEntityBean bean = HumanEntityBeanImpl.deserialize(FULFILLED_MAP, BeanSerializerImpl.getInstance());

        assertEquals(FULFILLED, bean);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = HumanEntityBeanImpl.serialize(EMPTY, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        HumanEntityBean bean = HumanEntityBeanImpl.deserialize(EMPTY_MAP, BeanSerializerImpl.getInstance());

        assertEquals(EMPTY, bean);
    }
}
