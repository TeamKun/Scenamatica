package org.kunlab.scenamatica.structures.minecraft.inventory;

import lombok.SneakyThrows;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;
import org.kunlab.scenamatica.structures.minecraft.StructureSerializerMock;
import org.kunlab.scenamatica.structures.minecraft.utils.MapTestUtil;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ItemStackStructureSerializeTest
{
    @SuppressWarnings("deprecation")
    public static final ItemStackStructure FULFILLED = new ItemStackStructureImpl(
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
            new HashMap<Enchantment, Integer>(),
            Arrays.asList(
                    ItemFlag.HIDE_ATTRIBUTES,
                    ItemFlag.HIDE_ENCHANTS,
                    ItemFlag.HIDE_DESTROYS,
                    ItemFlag.HIDE_PLACED_ON,
                    ItemFlag.HIDE_POTION_EFFECTS,
                    ItemFlag.HIDE_UNBREAKABLE
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
        /*this.put("enchants", new HashMap<String, Integer>()
        {{
            this.put("power", 1);
            this.put("flame", 1);
            this.put("infinity", 4);
            this.put("punch", 5);
            this.put("fortune", 1);
            this.put("knockback", 4);

        }});*/
        this.put("flags", Arrays.asList(
                "HIDE_ATTRIBUTES",
                "HIDE_ENCHANTS",
                "HIDE_DESTROYS",
                "HIDE_PLACED_ON",
                "HIDE_POTION_EFFECTS",
                "HIDE_UNBREAKABLE"
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

    public static final ItemStackStructure EMPTY = new ItemStackStructureImpl(
            null,
            null,
            null,
            null,
            Collections.emptyList(),
            Collections.emptyMap(),
            Collections.emptyList(),
            null,
            Collections.emptyMap(),
            Collections.emptyList(),
            Collections.emptyList(),
            null
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    private static final ItemStackStructureImpl ONLY_ONE_ITEM = new ItemStackStructureImpl(
            Material.DIAMOND_HOE,
            null,
            null,
            null,
            Collections.emptyList(),
            Collections.emptyMap(),
            Collections.emptyList(),
            null,
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
        Map<String, Object> actual = ItemStackStructureImpl.serialize(FULFILLED, StructureSerializerMock.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, actual);
    }

    @Test
    void 正常デシリアライズできるか()
    {
        ItemStackStructure actual = ItemStackStructureImpl.deserialize(FULFILLED_MAP);

        assertEquals(FULFILLED, actual);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> actual = ItemStackStructureImpl.serialize(EMPTY, StructureSerializerMock.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, actual);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        ItemStackStructure actual = ItemStackStructureImpl.deserialize(EMPTY_MAP);

        assertEquals(EMPTY, actual);
    }

    @Test
    void 個数が1のときに省略してシリアライズできるか()
    {
        Map<String, Object> actual = ItemStackStructureImpl.serialize(ONLY_ONE_ITEM, StructureSerializerMock.getInstance());

        MapTestUtil.assertEqual(ONLY_ONE_ITEM_MAP, actual);
    }

    @Test
    void 個数が1のときに省略してデシリアライズできるか()
    {
        ItemStackStructure actual = ItemStackStructureImpl.deserialize(ONLY_ONE_ITEM_MAP);

        assertEquals(ONLY_ONE_ITEM, actual);
    }

    @Test
    void 属性のgenericを省略してデシアライズできるか()
    {
        UUID attributeUUID = UUID.randomUUID();

        ItemStackStructure structure = new ItemStackStructureImpl(
                Material.DIAMOND_HOE,
                null,
                null,
                null,
                Collections.emptyList(),
                Collections.emptyMap(),
                Collections.emptyList(),
                null,
                new HashMap<Attribute, List<AttributeModifier>>()
                {{
                    this.put(
                            Attribute.GENERIC_ATTACK_DAMAGE,
                            Collections.singletonList(new AttributeModifier(
                                            attributeUUID,
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
                            this.put("uuid", attributeUUID.toString());
                            this.put("name", "generic.attackDamage");
                            this.put("amount", 1.0);
                            this.put("operation", "ADD_NUMBER");
                        }})
                );
            }});
        }};

        ItemStackStructure actual = ItemStackStructureImpl.deserialize(map);

        assertEquals(structure, actual);
    }
}
