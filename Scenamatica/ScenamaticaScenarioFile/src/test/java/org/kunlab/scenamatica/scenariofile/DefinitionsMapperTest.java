package org.kunlab.scenamatica.scenariofile;

import org.junit.jupiter.api.Test;
import org.kunlab.scenamatica.scenariofile.structures.utils.MapTestUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DefinitionsMapperTest
{
    private static final Map<String, Object> SCHEMA_DEF = new HashMap<String, Object>()
    {{
        Map<String, Object> dataTypes = new HashMap<>();
        dataTypes.put("string", "string");
        dataTypes.put("int", 0);
        dataTypes.put("double", 0.0);
        dataTypes.put("boolean", true);
        dataTypes.put("long", 0L);
        dataTypes.put("float", 0.0f);

        this.put("deeps", dataTypes);
        this.put("list", Arrays.asList(dataTypes, dataTypes, dataTypes));

    }};

    @Test
    void オブジェクトの置換ができるか()
    {
        Map<String, Object> target = new HashMap<String, Object>()
        {{
            this.put("$ref", "deeps");
        }};
        // noinspection unchecked
        Map<String, Object> expected = new HashMap<>((Map<String, Object>) SCHEMA_DEF.get("deeps"));

        DefinitionsMapper.resolveReferences(target, SCHEMA_DEF);
        MapTestUtil.assertEqual(target, expected);
    }

    @Test
    void リストの置換ができるか()
    {
        Map<String, Object> target = new HashMap<String, Object>()
        {{
            this.put("$ref", "list");
        }};
        List<Map<String, Object>> expected = new ArrayList<>((List<Map<String, Object>>) SCHEMA_DEF.get("list"));

        List<Map<String, Object>> value = (List<Map<String, Object>>) DefinitionsMapper.resolveReferences(target, SCHEMA_DEF);

        MapTestUtil.assertEqual(value, expected);
    }

    @Test
    void 深いオブジェクトの置換ができるか()
    {
        Map<String, Object> target = new HashMap<String, Object>()
        {{
            this.put("deeps", new HashMap<String, Object>()
            {{
                this.put("more", new HashMap<String, Object>()
                {{
                    this.put("$ref", "deeps");
                }});
            }});
        }};

        Map<String, Object> expected = new HashMap<String, Object>()
        {{
            this.put("deeps", new HashMap<String, Object>()
            {{
                this.put("more", new HashMap<>((Map<String, Object>) SCHEMA_DEF.get("deeps")));
            }});
        }};

        DefinitionsMapper.resolveReferences(target, SCHEMA_DEF);
        MapTestUtil.assertEqual(expected, target);
    }

}
