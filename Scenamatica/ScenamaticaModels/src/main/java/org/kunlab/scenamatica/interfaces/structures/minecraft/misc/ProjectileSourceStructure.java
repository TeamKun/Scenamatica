package org.kunlab.scenamatica.interfaces.structures.minecraft.misc;

import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.interfaces.scenariofile.Structure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;

/**
 * 投射物の発射元を表すインターフェースです。
 */
@TypeDoc(
        name = "ProjectileSource",
        description = "投射物の発射元を表します。",
        extending = EntitySpecifier.class
)
public interface ProjectileSourceStructure extends Structure
{
}
