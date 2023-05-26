package net.kunmc.lab.scenamatica.scenariofile;

import org.yaml.snakeyaml.constructor.*;
import org.yaml.snakeyaml.nodes.*;

import java.util.*;

@SuppressWarnings("VulnerableCodeUsages")
public class SimpleYamlConstructor extends SafeConstructor
{
    private static final Map<String, Boolean> BOOLEAN = new HashMap<>();

    static
    {
        BOOLEAN.put("true", true);
        BOOLEAN.put("yes", true);
        BOOLEAN.put("on", true);

        BOOLEAN.put("false", false);
        BOOLEAN.put("no", false);
        BOOLEAN.put("off", false);
    }

    public SimpleYamlConstructor()
    {
        super();
        this.yamlConstructors.put(Tag.BOOL, new CustomBooleanConstruct());
    }

    public class CustomBooleanConstruct extends AbstractConstruct
    {
        @Override
        public Object construct(Node node)
        {
            assert node instanceof ScalarNode;
            String val = SimpleYamlConstructor.this.constructScalar((ScalarNode) node);
            if (node.getStartMark().getColumn() == 0 && val.equalsIgnoreCase("on"))
                return val;

            return BOOLEAN.get(val.toLowerCase());
        }
    }
}
