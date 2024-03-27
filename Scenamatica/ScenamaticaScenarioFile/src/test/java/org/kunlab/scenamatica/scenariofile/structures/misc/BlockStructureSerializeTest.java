package org.kunlab.scenamatica.scenariofile.structures.misc;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BlockStructureSerializeTest
{
    public static final BlockStructure FULFILLED = new BlockStructureImpl(
            Material.MAGENTA_WALL_BANNER,
            LocationStructureImpl.of(new Location(null, 114, 514, 19)),
            new HashMap<String, Object>()
            {{
                this.put("key", "value");
            }},
            15,
            Biome.BADLANDS,
            new HashMap<String, Object>()
            {{
                this.put("key", "value");
            }},
            (byte) 24
    );

    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put("type", "MAGENTA_WALL_BANNER");
        this.put("location", new HashMap<String, Object>()
        {{
            this.put("x", 114.0);
            this.put("y", 514.0);
            this.put("z", 19.0);
        }});
        this.put("metadata", new HashMap<String, Object>()
        {{
            this.put("key", "value");
        }});
        this.put("light", 15);
        this.put("biome", "BADLANDS");
        this.put("blockData", new HashMap<String, Object>()
        {{
            this.put("key", "value");
        }});
        this.put("blockState", (byte) 24);
    }};

    public static final BlockStructure EMPTY = new BlockStructureImpl(
            null,
            null,
            Collections.emptyMap(),
            null,
            null,
            Collections.emptyMap(),
            null
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = BlockStructureImpl.serialize(FULFILLED, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        BlockStructure structure = BlockStructureImpl.deserialize(FULFILLED_MAP, StructureSerializerImpl.getInstance());

        assertEquals(structure, FULFILLED);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = BlockStructureImpl.serialize(EMPTY, StructureSerializerImpl.getInstance());
        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        BlockStructure structure = BlockStructureImpl.deserialize(EMPTY_MAP, StructureSerializerImpl.getInstance());
        assertEquals(EMPTY, structure);
    }
}
