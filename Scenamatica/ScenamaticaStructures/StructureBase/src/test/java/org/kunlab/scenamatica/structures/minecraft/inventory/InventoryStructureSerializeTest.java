package org.kunlab.scenamatica.structures.minecraft.inventory;

import org.bukkit.Material;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.InventoryStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.inventory.ItemStackStructure;
import org.kunlab.scenamatica.structures.minecraft.StructureSerializerMock;
import org.kunlab.scenamatica.structures.minecraft.inventory.InventoryStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.inventory.ItemStackStructureImpl;
import org.kunlab.scenamatica.structures.minecraft.utils.MapTestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class InventoryStructureSerializeTest
{
    public static final InventoryStructure FULFILLED = new InventoryStructureImpl(
            30,
            "This is literally an inventory",
            new HashMap<Integer, ItemStackStructure>()
            {{
                this.put(1, new ItemStackStructureImpl(Material.DIAMOND, 1));
                this.put(2, new ItemStackStructureImpl(Material.DIAMOND_BOOTS, 2));
                this.put(3, new ItemStackStructureImpl(Material.DIAMOND_CHESTPLATE, 3));
            }}
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        StructureSerializer serializer = StructureSerializerMock.getInstance();

        this.put("size", 30);
        this.put("title", "This is literally an inventory");
        this.put("items", new HashMap<Integer, Map<String, Object>>()
        {{
            this.put(1, serializer.serialize(new ItemStackStructureImpl(Material.DIAMOND, 1), ItemStackStructure.class));
            this.put(2, serializer.serialize(new ItemStackStructureImpl(Material.DIAMOND_BOOTS, 2), ItemStackStructure.class));
            this.put(3, serializer.serialize(new ItemStackStructureImpl(Material.DIAMOND_CHESTPLATE, 3), ItemStackStructure.class));
        }});
    }};

    public static final InventoryStructure EMPTY = new InventoryStructureImpl(
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
        Map<String, Object> actual = InventoryStructureImpl.serialize(FULFILLED, StructureSerializerMock.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, actual);
    }

    @Test
    void 正常デシリアライズできるか()
    {
        InventoryStructure actual = InventoryStructureImpl.deserialize(FULFILLED_MAP, StructureSerializerMock.getInstance());

        assert FULFILLED.equals(actual);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> actual = InventoryStructureImpl.serialize(EMPTY, StructureSerializerMock.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, actual);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        InventoryStructure actual = InventoryStructureImpl.deserialize(EMPTY_MAP, StructureSerializerMock.getInstance());

        assert EMPTY.equals(actual);
    }
}
