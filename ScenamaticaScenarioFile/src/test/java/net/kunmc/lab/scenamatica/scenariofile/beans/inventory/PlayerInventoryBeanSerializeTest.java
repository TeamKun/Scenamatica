package net.kunmc.lab.scenamatica.scenariofile.beans.inventory;

import net.kunmc.lab.scenamatica.scenariofile.beans.utils.MapTestUtil;
import net.kunmc.lab.scenamatica.scenariofile.interfaces.inventory.ItemStackBean;
import net.kunmc.lab.scenamatica.scenariofile.interfaces.inventory.PlayerInventoryBean;
import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerInventoryBeanSerializeTest
{
    public static final PlayerInventoryBean FULFILLED = new PlayerInventoryBeanImpl(
            new InventoryBeanImpl(
                    36,
                    "This is main inventory",
                    new HashMap<Integer, ItemStackBean>()
                    {{
                        this.put(0, new ItemStackBeanImpl(Material.STONE, 1));
                        this.put(1, new ItemStackBeanImpl(Material.DIRT, 2));
                    }}
            ),
            new ItemStackBeanImpl(Material.DIAMOND, 3),
            new ItemStackBeanImpl(Material.DIAMOND, 3),
            new ItemStackBeanImpl[]{
                    new ItemStackBeanImpl(Material.GOLDEN_APPLE, 4),
                    new ItemStackBeanImpl(Material.GOLDEN_CARROT, 5),
                    null,
                    new ItemStackBeanImpl(Material.GOLDEN_HELMET, 7),
            }
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("main", new HashMap<String, Object>()
        {{
            this.put("title", "This is main inventory");
            this.put("items", new HashMap<Integer, Object>()
            {{
                this.put(0, new HashMap<String, Object>()
                {{
                    this.put("type", "STONE");
                }});
                this.put(1, new HashMap<String, Object>()
                {{
                    this.put("type", "DIRT");
                    this.put("amount", 2);
                }});
            }});
        }});
        this.put("mainHandItem", new HashMap<String, Object>()
        {{
            this.put("type", "DIAMOND");
            this.put("amount", 3);
        }});
        this.put("offHandItem", new HashMap<String, Object>()
        {{
            this.put("type", "DIAMOND");
            this.put("amount", 3);
        }});
        this.put("armors", Arrays.asList(
                new HashMap<String, Object>()
                {{
                    this.put("type", "GOLDEN_APPLE");
                    this.put("amount", 4);
                }},
                new HashMap<String, Object>()
                {{
                    this.put("type", "GOLDEN_CARROT");
                    this.put("amount", 5);
                }},
                null,
                new HashMap<String, Object>()
                {{
                    this.put("type", "GOLDEN_HELMET");
                    this.put("amount", 7);
                }}
        ));
    }};

    public static final PlayerInventoryBeanImpl EMPTY = new PlayerInventoryBeanImpl(
            InventoryBeanSerializeTest.EMPTY,
            null,
            null,
            new ItemStackBeanImpl[]{
                    null,
                    null,
                    null,
                    null,
            }
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    @Test
    void 正常シリアライズできるか()
    {
        Map<String, Object> actual = PlayerInventoryBeanImpl.serialize(FULFILLED);

        MapTestUtil.assertEqual(FULFILLED_MAP, actual);
    }

    @Test
    void 正常デシリアライズできるか()
    {
        PlayerInventoryBean actual = PlayerInventoryBeanImpl.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, actual);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> actual = PlayerInventoryBeanImpl.serialize(EMPTY);

        MapTestUtil.assertEqual(EMPTY_MAP, actual);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        PlayerInventoryBean actual = PlayerInventoryBeanImpl.deserialize(EMPTY_MAP);

        assertEquals(EMPTY, actual);
    }

    @Test
    void 防具の数がおかしい場合はエラーか()
    {
        Map<String, Object> map = new HashMap<String, Object>()
        {{
            this.put("main", new HashMap<String, Object>()
            {{
                this.put("size", 36);
                this.put("title", "This is main inventory");
                this.put("items", Collections.emptyMap());
            }});
            this.put("armors", Arrays.asList(
                    new HashMap<String, Object>()
                    {{
                        this.put("type", "GOLDEN_APPLE");
                        this.put("amount", 4);
                    }},
                    new HashMap<String, Object>()
                    {{
                        this.put("type", "GOLDEN_CARROT");
                        this.put("amount", 5);
                    }},
                    null,
                    new HashMap<String, Object>()
                    {{
                        this.put("type", "GOLDEN_HELMET");
                        this.put("amount", 7);
                    }},
                    null  // 5個あるのでエラーになるはず。
            ));
        }};

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PlayerInventoryBeanImpl.deserialize(map)
        );

        // noinspection unchecked
        List<HashMap<String, Object>> armors = new ArrayList<>(((List<HashMap<String, Object>>) map.get("armors")));
        armors.remove(0);
        armors.remove(0);  // 3個になるので足りない。エラーになるはず。
        map.put("armors", armors);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PlayerInventoryBeanImpl.deserialize(map)
        );
    }
}
