package net.kunmc.lab.scenamatica.scenariofile.beans.inventory;

import net.kunmc.lab.scenamatica.interfaces.scenariofile.inventory.InventoryBean;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import org.bukkit.Material;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InventoryBeanSerializeTest
{
    public static final InventoryBean FULFILLED = new InventoryBeanImpl(
            30,
            "This is literally an inventory",
            new HashMap<Integer, ItemStackBean>()
            {{
                this.put(1, new ItemStackBeanImpl(Material.DIAMOND, 1));
                this.put(2, new ItemStackBeanImpl(Material.DIAMOND_BOOTS, 2));
                this.put(3, new ItemStackBeanImpl(Material.DIAMOND_CHESTPLATE, 3));
            }}
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("size", 30);
        this.put("title", "This is literally an inventory");
        this.put("items", new HashMap<Integer, Map<String, Object>>()
        {{
            this.put(1, ItemStackBeanImpl.serialize(new ItemStackBeanImpl(Material.DIAMOND, 1)));
            this.put(2, ItemStackBeanImpl.serialize(new ItemStackBeanImpl(Material.DIAMOND_BOOTS, 2)));
            this.put(3, ItemStackBeanImpl.serialize(new ItemStackBeanImpl(Material.DIAMOND_CHESTPLATE, 3)));
        }});
    }};

    public static final InventoryBean EMPTY = new InventoryBeanImpl(
            1,
            null,
            Collections.emptyMap()
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>()
    {{
        this.put("size", 1);
    }};

    @Test
    void 正常シリアライズできるか()
    {
        Map<String, Object> actual = InventoryBeanImpl.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, actual);
    }

    @Test
    void 正常デシリアライズできるか()
    {
        InventoryBean actual = InventoryBeanImpl.deserialize(FULFILLED_MAP);

        assert FULFILLED.equals(actual);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> actual = InventoryBeanImpl.serialize(EMPTY);

        MapTestUtil.assertEqual(EMPTY_MAP, actual);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        InventoryBean actual = InventoryBeanImpl.deserialize(EMPTY_MAP);

        assert EMPTY.equals(actual);
    }
}
