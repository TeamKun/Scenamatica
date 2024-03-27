package org.kunlab.scenamatica.scenariofile.structures;

import net.kunmc.lab.peyangpaperutils.versioning.Version;
import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.interfaces.scenariofile.VersionRange;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class VersionRangeSerializeTest
{
    public static final VersionRange FULLFILLED = new VersionRangeImpl(
            Version.of("1.0.0"),
            Version.of("2.0.0")
    );

    public static final Map<String, Object> FULLFILLED_MAP = new HashMap<String, Object>()
    {{
        this.put(VersionRange.KEY_SINCE, "1.0.0");
        this.put(VersionRange.KEY_UNTIL, "2.0.0");
    }};

    public static final VersionRange EMPTY = new VersionRangeImpl(
            null,
            null
    );

    public static final Map<String, Object> EMPTY_MAP = new HashMap<String, Object>();

    @Test
    void 正常にシリアライズできるか()
    {
        Map<String, Object> map = VersionRangeImpl.serialize(FULLFILLED);

        MapTestUtil.assertEqual(FULLFILLED_MAP, map);
    }

    @Test
    void 正常にデシリアライズできるか()
    {
        VersionRange range = VersionRangeImpl.deserialize(FULLFILLED_MAP);

        assertEquals(FULLFILLED, range);
    }

    @Test
    void 必須項目のみでシリアライズできるか()
    {
        Map<String, Object> map = VersionRangeImpl.serialize(EMPTY);

        MapTestUtil.assertEqual(EMPTY_MAP, map);
    }

    @Test
    void 必須項目のみでデシリアライズできるか()
    {
        VersionRange range = VersionRangeImpl.deserialize(EMPTY_MAP);

        assertEquals(EMPTY, range);
    }
}
