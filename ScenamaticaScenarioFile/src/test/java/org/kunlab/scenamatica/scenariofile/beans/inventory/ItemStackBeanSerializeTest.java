package org.kunlab.scenamatica.scenariofile.beans.inventory;

import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.inventory.ItemStackBean;
import org.kunlab.scenamatica.scenariofile.BeanSerializerImpl;
import org.kunlab.scenamatica.scenariofile.beans.utils.MapTestUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemStackBeanSerializeTest
{
    @SuppressWarnings("deprecation")
    public static final ItemStackBean FULFILLED = new ItemStackBeanImpl(
            Material.DIAMOND_HOE,
            2,
            "The test legendary diamond hoe",
            "This is literally a test localized name",
            Arrays.asList(
                    "This is a test lore.",
                    "This is a test lore too.",
                    "This is also a test lore.",
                    "This is literally a test lore."
            ),
            114514,
            new HashMap<Enchantment, Integer>()
            {{
                this.put(Enchantment.ARROW_DAMAGE, 1);
                this.put(Enchantment.ARROW_FIRE, 1);
                this.put(Enchantment.ARROW_INFINITE, 4);
                this.put(Enchantment.ARROW_KNOCKBACK, 5);
                this.put(Enchantment.LOOT_BONUS_BLOCKS, 1);
                this.put(Enchantment.KNOCKBACK, 4);
            }},
            Arrays.asList(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_DESTROYS,
                    ItemFlag.HIDE_PLACED_ON,
                    ItemFlag.HIDE_POTION_EFFECTS,
                    ItemFlag.HIDE_UNBREAKABLE,
                    ItemFlag.HIDE_DYE
            ),
            true,
            new HashMap<Attribute, List<AttributeModifier>>()
            {{
                this.put(
                        Attribute.GENERIC_ATTACK_DAMAGE,
                        Collections.singletonList(new AttributeModifier(
                                "generic.attackDamage",
                                1,
                                AttributeModifier.Operation.ADD_NUMBER
                        ))
                );
            }},
            Arrays.asList(new NamespacedKey("test", "test"), new NamespacedKey("test", "test2")),
            Arrays.asList(new NamespacedKey("test", "test"), new NamespacedKey("test", "test2")),
            100
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("type", "DIAMOND_HOE");
        this.put("amount", 2);
        this.put("name", "The test legendary diamond hoe");
        this.put("localizedName", "This is literally a test localized name");
        this.put("lores", Arrays.asList(
                "This is a test lore.",
                "This is a test lore too.",
                "This is also a test lore.",
                "This is literally a test lore."
        ));
        this.put("customModel", 114514);
        this.put("enchants", new HashMap<String, Integer>()
        {{
            this.put("power", 1);
            this.put("flame", 1);
            this.put("infinity", 4);
            this.put("punch", 5);
            this.put("fortune", 1);
            this.put("knockback", 4);

        }});
        this.put("flags", Arrays.asList(
                "HIDE_ATTRIBUTES",
                "HIDE_ENCHANTS",
                "HIDE_DESTROYS",
                "HIDE_PLACED_ON",
                "HIDE_POTION_EFFECTS",
                "HIDE_UNBREAKABLE",
                "HIDE_DYE"
        ));
        this.put("unbreakable", true);
        this.put("attributes", new HashMap<String, List<Map<String, Object>>>()
        {{
            this.put("attack_damage", Collections.singletonList(
                    new HashMap<String, Object>()
                    {{
                        this.put("name", "generic.attackDamage");
                        this.put("amount", 1.0);
                        this.put("operation", "ADD_NUMBER");
                    }}
            ));
        }});
        this.put("placeables", Arrays.asList("test:test", "test:test2"));
        this.put("destroyables", Arrays.asList("test:test", "test:test2"));
        this.put("damage", 100);
    }};

    public static final ItemStackBean EMPTY = new ItemStackBeanImpl(
            null,
            null,
            null,
            null,
            Collections.emptyList(),
            null,
            Collections.emptyMap(),
            Collections.emptyList(),
            false,
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyList(),
            null
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    private static final ItemStackBeanImpl ONLY_ONE_ITEM = new ItemStackBeanImpl(
            Material.DIAMOND_HOE,
            null,
            null,
            null,
            Collections.emptyList(),
            null,
            Collections.emptyMap(),
            Collections.emptyList(),
            false,
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyList(),
            null
    );

    private static final Map<String, Object> ONLY_ONE_ITEM_MAP = new HashMap<String, Object>()
    {{
        this.put("type", "DIAMOND_HOE");
    }};

    @BeforeAll
    @SuppressWarnings("unchecked")
    @SneakyThrows({NoSuchFieldException.class, IllegalAccessException.class})
    static void initEnchants()
    {
        Map<String, Enchantment> byKey;
        Map<String, Enchantment> byName;
        Field fByKey = Enchantment.class.getDeclaredField("byKey");
        Field fByName = Enchantment.class.getDeclaredField("byName");
        fByKey.setAccessible(true);
        fByName.setAccessible(true);

        byKey = (Map<String, Enchantment>) fByKey.get(null);
        byName = (Map<String, Enchantment>) fByName.get(null);


        byKey.put("power", Enchantment.ARROW_DAMAGE);
        byKey.put("flame", Enchantment.ARROW_FIRE);
        byKey.put("infinity", Enchantment.ARROW_INFINITE);
        byKey.put("punch", Enchantment.ARROW_KNOCKBACK);
        byKey.put("fortune", Enchantment.LOOT_BONUS_BLOCKS);
        byKey.put("knockback", Enchantment.KNOCKBACK);

        byName.put("power", Enchantment.ARROW_DAMAGE);
        byName.put("flame", Enchantment.ARROW_FIRE);
        byName.put("infinity", Enchantment.ARROW_INFINITE);
        byName.put("punch", Enchantment.ARROW_KNOCKBACK);
        byName.put("fortune", Enchantment.LOOT_BONUS_BLOCKS);
        byName.put("knockback", Enchantment.KNOCKBACK);
    }

    @Test
    void 正常シリアライズできるか()
    {
        Map<String, Object> actual = ItemStackBeanImpl.serialize(FULFILLED, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, actual);
    }

    @Test
    void 正常デシリアライズできるか()
    {
        ItemStackBean actual = ItemStackBeanImpl.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, actual);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> actual = ItemStackBeanImpl.serialize(EMPTY, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, actual);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ItemStackBean actual = ItemStackBeanImpl.deserialize(EMPTY_MAP);

        assertEquals(EMPTY, actual);
    }

    @Test
    void 個数が1のときに省略してシリアライズできるか()
    {
        Map<String, Object> actual = ItemStackBeanImpl.serialize(ONLY_ONE_ITEM, BeanSerializerImpl.getInstance());

        MapTestUtil.assertEqual(ONLY_ONE_ITEM_MAP, actual);
    }

    @Test
    void 個数が1のときに省略してデシリアライズできるか()
    {
        ItemStackBean actual = ItemStackBeanImpl.deserialize(ONLY_ONE_ITEM_MAP);

        assertEquals(ONLY_ONE_ITEM, actual);
    }

    @Test
    void 属性のgenericを省略してデシアライズできるか()
    {
        ItemStackBean bean = new ItemStackBeanImpl(
                Material.DIAMOND_HOE,
                1,
                null,
                null,
                Collections.emptyList(),
                null,
                Collections.emptyMap(),
                Collections.emptyList(),
                false,
                new HashMap<Attribute, List<AttributeModifier>>()
                {{
                    this.put(
                            Attribute.GENERIC_ATTACK_DAMAGE,
                            Collections.singletonList(new AttributeModifier(
                                            "generic.attackDamage",
                                            1,
                                            AttributeModifier.Operation.ADD_NUMBER
                                    )
                            )
                    );
                }},
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );

        Map<String, Object> map = new HashMap<String, Object>()
        {{
            this.put("type", "DIAMOND_HOE");
            this.put("attributes", new HashMap<String, List<Map<String, Object>>>()
            {{
                this.put("attack_damage", Collections.singletonList(
                        new HashMap<String, Object>()
                        {{
                            this.put("name", "generic.attackDamage");
                            this.put("amount", 1.0);
                            this.put("operation", "ADD_NUMBER");
                        }})
                );
            }});
        }};

        ItemStackBean actual = ItemStackBeanImpl.deserialize(map);

        assertEquals(bean, actual);
    }

    @Test
    void エンチャントがキーでもデシリアライズできるか()
    {
        ItemStackBean bean = new ItemStackBeanImpl(
                Material.DIAMOND_HOE,
                1,
                null,
                null,
                Collections.emptyList(),
                null,
                new HashMap<Enchantment, Integer>()
                {{
                    this.put(Enchantment.ARROW_DAMAGE, 1);
                    this.put(Enchantment.ARROW_FIRE, 1);
                    this.put(Enchantment.ARROW_INFINITE, 4);
                    this.put(Enchantment.ARROW_KNOCKBACK, 5);
                    this.put(Enchantment.LOOT_BONUS_BLOCKS, 1);
                    this.put(Enchantment.KNOCKBACK, 4);
                }},
                Collections.emptyList(),
                false,
                Collections.emptyMap(),
                Collections.emptyList(),
                Collections.emptyList(),
                null
        );

        Map<String, Object> map = new HashMap<String, Object>()
        {{
            this.put("type", "DIAMOND_HOE");
            this.put("enchants", new HashMap<String, Integer>()
            {{
                this.put(Enchantment.ARROW_DAMAGE.getKey().getKey(), 1);
                this.put(Enchantment.ARROW_FIRE.getKey().getKey(), 1);
                this.put(Enchantment.ARROW_INFINITE.getKey().getKey(), 4);
                this.put(Enchantment.ARROW_KNOCKBACK.getKey().getKey(), 5);
                this.put(Enchantment.LOOT_BONUS_BLOCKS.getKey().getKey(), 1);
                this.put(Enchantment.KNOCKBACK.getKey().getKey(), 4);
            }});
        }};

        ItemStackBean actual = ItemStackBeanImpl.deserialize(map);

        assertEquals(bean, actual);
    }


}
