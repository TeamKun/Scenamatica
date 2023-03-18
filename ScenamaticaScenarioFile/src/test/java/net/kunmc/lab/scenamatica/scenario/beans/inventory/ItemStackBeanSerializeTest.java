package net.kunmc.lab.scenamatica.scenario.beans.inventory;

import lombok.SneakyThrows;
import net.kunmc.lab.scenamatica.scenario.beans.utils.MapTestUtil;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemStackBeanSerializeTest
{
    @SuppressWarnings("deprecation")
    private final ItemStackBean fulfilled = new ItemStackBean(
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
            new HashMap<Attribute, AttributeModifier>()
            {{
                this.put(
                        Attribute.GENERIC_ATTACK_DAMAGE,
                        new AttributeModifier(
                                "generic.attackDamage",
                                1,
                                AttributeModifier.Operation.ADD_NUMBER
                        )
                );
            }},
            Arrays.asList(new NamespacedKey("test", "test"), new NamespacedKey("test", "test2")),
            Arrays.asList(new NamespacedKey("test", "test"), new NamespacedKey("test", "test2")),
            100
    );

    private final Map<String, Object> fulfilledMap = new HashMap<String, Object>()
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
        this.put("attributes", new HashMap<String, Map<String, Object>>()
        {{
            this.put("attack_damage", new HashMap<String, Object>()
            {{
                this.put("name", "generic.attackDamage");
                this.put("amount", 1.0);
                this.put("operation", "ADD_NUMBER");
            }});
        }});
        this.put("placeables", Arrays.asList("test:test", "test:test2"));
        this.put("destroyables", Arrays.asList("test:test", "test:test2"));
        this.put("damage", 100);
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
        Map<String, Object> actual = ItemStackBean.serialize(this.fulfilled);

        MapTestUtil.assertEqual(this.fulfilledMap, actual);
    }

    @Test
    void 正常デシリアライズできるか()
    {
        ItemStackBean actual = ItemStackBean.deserialize(this.fulfilledMap);

        assertEquals(this.fulfilled, actual);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        ItemStackBean bean = new ItemStackBean(Material.DIAMOND_HOE);
        Map<String, Object> actual = ItemStackBean.serialize(bean);

        Map<String, Object> expected = new HashMap<String, Object>()
        {{
            this.put("type", "DIAMOND_HOE");
        }};

        MapTestUtil.assertEqual(expected, actual);
    }

    @Test
    void 属性のgenericを省略してデシアライズできるか()
    {
        ItemStackBean bean = new ItemStackBean(
                Material.DIAMOND_HOE,
                1,
                null,
                null,
                Collections.emptyList(),
                null,
                Collections.emptyMap(),
                Collections.emptyList(),
                false,
                new HashMap<Attribute, AttributeModifier>()
                {{
                    this.put(
                            Attribute.GENERIC_ATTACK_DAMAGE,
                            new AttributeModifier(
                                    "generic.attackDamage",
                                    1,
                                    AttributeModifier.Operation.ADD_NUMBER
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
            this.put("attributes", new HashMap<String, Map<String, Object>>()
            {{
                this.put("attack_damage", new HashMap<String, Object>()
                {{
                    this.put("name", "generic.attackDamage");
                    this.put("amount", 1.0);
                    this.put("operation", "ADD_NUMBER");
                }});
            }});
        }};

        ItemStackBean actual = ItemStackBean.deserialize(map);

        assertEquals(bean, actual);
    }

    @Test
    void エンチャントがキーでもデシリアライズできるか()
    {
        ItemStackBean bean = new ItemStackBean(
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

        ItemStackBean actual = ItemStackBean.deserialize(map);

        assertEquals(bean, actual);
    }
}
