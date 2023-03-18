package net.kunmc.lab.scenamatica.scenario.beans.inventory;

import net.kunmc.lab.scenamatica.scenario.beans.utils.MapTestUtil;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class InventoryBeanTest
{
    private final InventoryBean fulfilledBean = new InventoryBean(
            30,
            "This is literally an inventory",
            new HashMap<Integer, ItemStackBean>()
            {{
                this.put(1, new ItemStackBean(Material.DIAMOND, 1));
                this.put(2, new ItemStackBean(Material.DIAMOND_BOOTS, 2));
                this.put(3, new ItemStackBean(Material.DIAMOND_CHESTPLATE, 3));
            }}
    );

    private final Map<String, Object> fulfilledMap = new HashMap<String, Object>()
    {{
        this.put("size", 30);
        this.put("title", "This is literally an inventory");
        this.put("items", new HashMap<Integer, Map<String, Object>>()
        {{
            this.put(1, ItemStackBean.serialize(new ItemStackBean(Material.DIAMOND, 1)));
            this.put(2, ItemStackBean.serialize(new ItemStackBean(Material.DIAMOND_BOOTS, 2)));
            this.put(3, ItemStackBean.serialize(new ItemStackBean(Material.DIAMOND_CHESTPLATE, 3)));
        }});
    }};

    @Test
    void 正常シリアライズできるか()
    {
        Map<String, Object> actual = InventoryBean.serialize(this.fulfilledBean);

        MapTestUtil.assertEqual(this.fulfilledMap, actual);
    }

    @Test
    void 正常デシリアライズできるか()
    {
        InventoryBean actual = InventoryBean.deserialize(this.fulfilledMap);

        assert this.fulfilledBean.equals(actual);
    }
}
