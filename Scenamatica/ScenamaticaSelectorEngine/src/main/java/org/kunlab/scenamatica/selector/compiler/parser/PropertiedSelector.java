package org.kunlab.scenamatica.selector.compiler.parser;

import lombok.Value;
import org.kunlab.scenamatica.selector.SelectorType;

import java.util.Map;

@Value
public class PropertiedSelector
{
    SelectorType type;
    Map<String, Object> properties;
}
