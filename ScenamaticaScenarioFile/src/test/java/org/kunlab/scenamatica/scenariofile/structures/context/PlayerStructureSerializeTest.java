package org.kunlab.scenamatica.scenariofile.structures.context;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.inventory.MainHand;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.context.PlayerStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.AEntityStructure;
import org.kunlab.scenamatica.scenariofile.StructureSerializerImpl;
import org.kunlab.scenamatica.scenariofile.structures.entity.LivingEntitySerializeTest;
import org.kunlab.scenamatica.scenariofile.structures.entity.entities.AHumanEntityStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.inventory.InventoryStructureSerializeTest;
import org.kunlab.scenamatica.scenariofile.structures.inventory.PlayerInventoryStructureSerializeTest;
import org.kunlab.scenamatica.scenariofile.structures.misc.LocationStructureImpl;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PlayerStructureSerializeTest
{
    public static final PlayerStructure FULFILLED;
    public static final Map<String, Object> FULFILLED_MAP = new HashMap<String, Object>(LivingEntitySerializeTest.FULFILLED_MAP)
    {{
        this.remove(AEntityStructure.KEY_TYPE);
        this.put("inventory", PlayerInventoryStructureSerializeTest.FULFILLED_MAP);
        this.put("enderChest", InventoryStructureSerializeTest.FULFILLED_MAP);
        this.put("mainHand", "LEFT");
        this.put("gamemode", "ADVENTURE");
        this.put("food", 20);

        this.put("name", "YajuSNPIName");
        this.put("online", false);
        this.put("connection", new HashMap<String, Object>()
        {{
            this.put("ip", "114.51.48.10");
            this.put("port", 1919);
            this.put("hostname", "YajuSNPIPC");
        }});
        this.put("display", "YajuSNPIDisplay");
        this.put("playerList", new HashMap<String, Object>()
        {{
            this.put("header", "YajuListHeader");
            this.put("footer", "YajuListFooter");
            this.put("name", "YajuSenpaiList");
        }});
        this.put("compass", new HashMap<String, Object>()
        {{
            this.put("x", 11.0);
            this.put("y", 45.0);
            this.put("z", 14.0);
        }});
        this.put("bedLocation", new HashMap<String, Object>()
        {{
            this.put("x", 11.0);
            this.put("y", 45.0);
            this.put("z", 14.0);
        }});
        this.put("exp", 20);
        this.put("level", 2000);
        this.put("totalExp", 20000);
        this.put("flyable", true);
        this.put("flying", true);
        this.put("walkSpeed", 114f);
        this.put("flySpeed", 514f);
        this.put("op", 4);
    }};
    public static final PlayerStructure EMPTY = new PlayerStructureImpl(
            new AHumanEntityStructureImpl(
                    LivingEntitySerializeTest.EMPTY,
                    null,
                    null,
                    null,
                    null,
                    null
            ),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            Collections.emptyList()
    );
    public static final Map<String, Object> EMPTY_MAP = new HashMap<>();

    static
    {
        try
        {
            FULFILLED = new PlayerStructureImpl(
                    new AHumanEntityStructureImpl(
                            LivingEntitySerializeTest.FULFILLED,
                            PlayerInventoryStructureSerializeTest.FULFILLED,
                            InventoryStructureSerializeTest.FULFILLED,
                            MainHand.LEFT,
                            GameMode.ADVENTURE,
                            20
                    ),
                    "YajuSNPIName",
                    false,
                    InetAddress.getByName("114.51.48.10"),
                    1919,
                    "YajuSNPIPC",
                    "YajuSNPIDisplay",
                    "YajuSenpaiList",
                    "YajuListHeader",
                    "YajuListFooter",
                    LocationStructureImpl.of(new Location(null, 11, 45, 14)),
                    LocationStructureImpl.of(new Location(null, 11, 45, 14)),
                    20,
                    2000,
                    20000,
                    true,
                    true,
                    114f,
                    514f,
                    4,
                    Collections.emptyList()
            );
        }
        catch (UnknownHostException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = PlayerStructureImpl.serialize(FULFILLED, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(FULFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        PlayerStructure structure = PlayerStructureImpl.deserialize(FULFILLED_MAP, StructureSerializerImpl.getInstance());

        assertEquals(FULFILLED, structure);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = PlayerStructureImpl.serialize(EMPTY, StructureSerializerImpl.getInstance());

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        PlayerStructure structure = PlayerStructureImpl.deserialize(EMPTY_MAP, StructureSerializerImpl.getInstance());

        assertEquals(EMPTY, structure);
    }
}
