package net.kunmc.lab.scenamatica.scenario.beans.inventory;

import net.kunmc.lab.scenamatica.scenario.beans.utils.MapTestUtil;
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
    private final PlayerInventoryBean fulfilledBean = new PlayerInventoryBean(
            new InventoryBean(
                    36,
                    "This is main inventory",
                    new HashMap<Integer, ItemStackBean>()
                    {{
                        this.put(0, new ItemStackBean(Material.STONE, 1));
                        this.put(1, new ItemStackBean(Material.DIRT, 2));
                    }}
            ),
            new ItemStackBean(Material.DIAMOND, 3),
            new ItemStackBean[]{
                    new ItemStackBean(Material.GOLDEN_APPLE, 4),
                    new ItemStackBean(Material.GOLDEN_CARROT, 5),
                    null,
                    new ItemStackBean(Material.GOLDEN_HELMET, 7),
            }
    );

    private final Map<String, Object> fulfilledMap = new HashMap<String, Object>()
    {{
        this.put("main", new HashMap<String, Object>()
        {{
            this.put("size", 36);
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
        this.put("offHand", new HashMap<String, Object>()
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

    @Test
    void 正常シリアライズできるか()
    {
        Map<String, Object> actual = PlayerInventoryBean.serialize(this.fulfilledBean);

        MapTestUtil.assertEqual(this.fulfilledMap, actual);
    }

    @Test
    void 正常デシリアライズできるか()
    {
        PlayerInventoryBean actual = PlayerInventoryBean.deserialize(this.fulfilledMap);

        assertEquals(this.fulfilledBean, actual);
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
                () -> PlayerInventoryBean.deserialize(map)
        );

        // noinspection unchecked
        List<HashMap<String, Object>> armors = new ArrayList<>(((List<HashMap<String, Object>>) map.get("armors")));
        armors.remove(0);
        armors.remove(0);  // 3個になるので足りない。エラーになるはず。
        map.put("armors", armors);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PlayerInventoryBean.deserialize(map)
        );
    }
}
