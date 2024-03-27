package org.kunlab.scenamatica.scenariofile.structures.misc;

import lombok.Value;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.SelectorProjectileSourceStructure;

@Value
public class SelectorProjectileSourceStructureImpl implements SelectorProjectileSourceStructure
{
    String selectorString;
}
