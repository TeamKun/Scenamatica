package org.kunlab.scenamatica.scenariofile.structures.entity;

import org.bukkit.GameMode;
import org.bukkit.inventory.MainHand;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.HumanEntityStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.entities.HumanEntityStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.inventory.InventoryStructureSerializeTest;
import org.kunlab.scenamatica.scenariofile.structures.inventory.PlayerInventoryStructureSerializeTest;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HumanEntityStructureSerializeTest
{
    public static final HumanEntityStructure FULFILLED = new HumanEntityStructureImpl(
            EntityStructureSerializeTest.FULFILLED,
            PlayerInventoryStructureSerializeTest.FULFILLED,
            InventoryStructureSerializeTest.FULFILLED,
            MainHand.LEFT,
            GameMode.ADVENTURE,
            20
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(EntityStructureSerializeTest.FULFILLED_MAP)
    {{
        this.remove(EntityStructure.KEY_TYPE);
        this.put("inventory", PlayerInventoryStructureSerializeTest.FULFILLED_MAP);
        this.put("enderChest", InventoryStructureSerializeTest.FULFILLED_MAP);
        this.put("mainHand", "LEFT");
        this.put("gamemode", "ADVENTURE");
        this.put("food", 20);
    }};

    public static final HumanEntityStructure EMPTY = new HumanEntityStructureImpl(
            EntityStructureSerializeTest.EMPTY,
            null,
            null,
            null,
            null,
            null
    );

    public static final Map<String, Object> EMPTY_MAP =
            new HashMap<>(EntityStructureSerializeTest.EMPTY_MAP);

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = HumanEntityStructureImpl.serialize(FULFILLED, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        HumanEntityStructure structure = HumanEntityStructureImpl.deserialize(FULFILLED_MAP, StructureSerializerImpl.getInstance());

        assertEquals(FULFILLED, structure);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = HumanEntityStructureImpl.serialize(EMPTY, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        HumanEntityStructure structure = HumanEntityStructureImpl.deserialize(EMPTY_MAP, StructureSerializerImpl.getInstance());

        assertEquals(EMPTY, structure);
    }
}
