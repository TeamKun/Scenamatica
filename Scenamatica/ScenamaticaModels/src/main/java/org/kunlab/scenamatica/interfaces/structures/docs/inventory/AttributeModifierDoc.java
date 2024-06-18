package org.kunlab.scenamatica.interfaces.structures.docs.inventory;

import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlot;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.TypeProperty;

import java.util.UUID;

@TypeDoc(
        name = "AttributeModifier",
        description = "属性の変更情報を格納します。",
        mappingOf = AttributeModifier.class,
        properties = {
                @TypeProperty(
                        name = "name",
                        description = "属性の変更名です。",
                        type = String.class
                ),
                @TypeProperty(
                        name = "amount",
                        description = "属性の変更量です。",
                        type = double.class
                ),
                @TypeProperty(
                        name = "operation",
                        description = "属性の変更方法です。",
                        type = AttributeModifier.Operation.class
                ),
                @TypeProperty(
                        name = "uuid",
                        description = "属性の変更のUUIDです。",
                        type = UUID.class
                ),
                @TypeProperty(
                        name = "slot",
                        description = "属性の変更のスロットです。",
                        type = EquipmentSlot.class
                )
        }
)
public interface AttributeModifierDoc
{
}
