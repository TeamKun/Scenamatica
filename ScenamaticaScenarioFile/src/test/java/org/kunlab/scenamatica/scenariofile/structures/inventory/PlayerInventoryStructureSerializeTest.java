package org.kunlab.scenamatica.scenariofile.structures.inventory;

import org.bukkit.Material;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.PlayerInventoryStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerInventoryStructureSerializeTest
{
    public static final PlayerInventoryStructure FULFILLED = new PlayerInventoryStructureImpl(
            new InventoryStructureImpl<>(
                    36,
                    "This is main inventory",
                    new HashMap<Integer, ItemStackStructure>()
                    {{
                        this.put(0, new ItemStackStructureImpl(Material.STONE, 1));
                        this.put(1, new ItemStackStructureImpl(Material.DIRT, 2));
                    }}
            ),
            new ItemStackStructureImpl(Material.DIAMOND, 3),
            new ItemStackStructureImpl(Material.DIAMOND, 3),
            new ItemStackStructureImpl[]{
                    new ItemStackStructureImpl(Material.GOLDEN_APPLE, 4),
                    new ItemStackStructureImpl(Material.GOLDEN_CARROT, 5),
                    null,
                    new ItemStackStructureImpl(Material.GOLDEN_HELMET, 7),
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
                    this.put("amount", 1);
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

    public static final PlayerInventoryStructureImpl EMPTY = new PlayerInventoryStructureImpl(
            InventoryStructureSerializeTest.EMPTY,
            null,
            null,
            new ItemStackStructureImpl[]{
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
        Map<String, Object> actual = PlayerInventoryStructureImpl.serializePlayerInventory(FULFILLED, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, actual);
    }

    @Test
    void 正常デシリアライズできるか()
    {
        PlayerInventoryStructure actual = PlayerInventoryStructureImpl.deserializePlayerInventory(FULFILLED_MAP, StructureSerializerImpl.getInstance());

        assertEquals(FULFILLED, actual);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> actual = PlayerInventoryStructureImpl.serializePlayerInventory(EMPTY, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, actual);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        PlayerInventoryStructure actual = PlayerInventoryStructureImpl.deserializePlayerInventory(EMPTY_MAP, StructureSerializerImpl.getInstance());

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
                () -> PlayerInventoryStructureImpl.deserializePlayerInventory(map, StructureSerializerImpl.getInstance())
        );

        // noinspection unchecked
        List<HashMap<String, Object>> armors = new ArrayList<>(((List<HashMap<String, Object>>) map.get("armors")));
        armors.remove(0);
        armors.remove(0);  // 3個になるので足りない。エラーになるはず。
        map.put("armors", armors);

        Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> PlayerInventoryStructureImpl.deserializePlayerInventory(map, StructureSerializerImpl.getInstance())
        );
    }
}
