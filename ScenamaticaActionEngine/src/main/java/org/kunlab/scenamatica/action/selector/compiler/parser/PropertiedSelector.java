package org.kunlab.scenamatica.action.selector.compiler.parser;

import lombok.Value;
import org.kunlab.scenamatica.action.selector.SelectorType;

import java.util.Map;

@Value
public class PropertiedSelector
{
    SelectorType type;
    Map<String, Object> properties;
}
