package org.kunlab.scenamatica.structures.minecraft.misc;

import lombok.Value;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.SelectorProjectileSourceStructure;

@Value
public class SelectorProjectileSourceStructureImpl implements SelectorProjectileSourceStructure
{
    String selectorString;
}
