package org.kunlab.scenamatica.structures.minecraft.entity;

import lombok.Value;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.YAMLNodeType;
import org.kunlab.scenamatica.exceptions.scenariofile.YamlParsingException;
import org.kunlab.scenamatica.interfaces.scenariofile.StructuredYamlNode;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.DamageStructure;
import org.kunlab.scenamatica.structures.StructureMappers;
import org.kunlab.scenamatica.structures.StructureValidators;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

@Value
@SuppressWarnings("deprecation")
public class DamageStructureImpl implements DamageStructure
{
    @NotNull
    Map<EntityDamageEvent.DamageModifier, Double> modifiers;
    @NotNull
    EntityDamageEvent.DamageCause cause;
    double damage;

    @NotNull
    public static Map<String, Object> serialize(@NotNull DamageStructure structure)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_DAMAGE, structure.getDamage());
        Map<String, Object> modifiers = new HashMap<>();
        for (Map.Entry<EntityDamageEvent.DamageModifier, Double> entry : structure.getModifiers().entrySet())
            modifiers.put(entry.getKey().name().toLowerCase(), entry.getValue());
        if (!modifiers.isEmpty())
            map.put(KEY_MODIFIER, modifiers);
        map.put(KEY_CAUSE, structure.getCause().name());

        return map;
    }

    public static boolean validate(@NotNull StructuredYamlNode node) throws YamlParsingException
    {
        node.get(KEY_CAUSE).validateIfExists(StructureValidators.enumName(EntityDamageEvent.DamageCause.class));
        node.get(KEY_DAMAGE).ensureTypeOf(YAMLNodeType.NUMBER);
        if (node.containsKey(KEY_MODIFIER))
        {
            StructuredYamlNode modifierNode = node.get(KEY_MODIFIER);
            modifierNode.ensureTypeOf(YAMLNodeType.MAPPING);
            modifierNode.validate(n -> {
                StructuredYamlNode.Validator enumNameValidator =
                        StructureValidators.enumName(EntityDamageEvent.DamageModifier.class);
                for (StructuredYamlNode key : n.keys())
                {
                    key.ensureTypeOf(YAMLNodeType.STRING);
                    key.validate(enumNameValidator);

                    n.get(key).ensureTypeOf(YAMLNodeType.NUMBER);
                }
                return null;
            });
        }

        return true;
    }

    @NotNull
    public static DamageStructure deserialize(@NotNull StructuredYamlNode node) throws YamlParsingException
    {
        validate(node);
        Map<EntityDamageEvent.DamageModifier, Double> modifiers = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
        if (node.containsKey(KEY_MODIFIER))
            for (Map.Entry<String, Object> entry : MapUtils.checkAndCastMap(node.get(KEY_MODIFIER)).entrySet())
                modifiers.put(EntityDamageEvent.DamageModifier.valueOf(entry.getKey().toUpperCase()), Double.parseDouble(entry.getValue().toString()));

        return new DamageStructureImpl(
                modifiers,
                node.get(KEY_CAUSE).getAs(
                        StructureMappers.enumName(EntityDamageEvent.DamageCause.class),
                        EntityDamageEvent.DamageCause.CUSTOM
                ),
                node.get(KEY_DAMAGE).asDouble()
        );
    }

    public static DamageStructure of(EntityDamageEvent event)
    {
        Map<EntityDamageEvent.DamageModifier, Double> modifiers = new HashMap<>();
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values())
            modifiers.put(modifier, event.getDamage(modifier));

        return new DamageStructureImpl(
                modifiers,
                event.getCause(),
                event.getDamage()
        );
    }

    public static boolean isApplicable(Object o)
    {
        return o instanceof EntityDamageEvent;
    }

    @Override
    public void applyTo(@NotNull EntityDamageEvent event)
    {
        for (Map.Entry<EntityDamageEvent.DamageModifier, Double> entry : this.modifiers.entrySet())
            event.setDamage(entry.getKey(), entry.getValue());
        event.setDamage(this.damage);
    }

    @Override
    public boolean isAdequate(@Nullable EntityDamageEvent event, boolean ignored)
    {
        if (event == null)
            return false;

        for (Map.Entry<EntityDamageEvent.DamageModifier, Double> entry : this.modifiers.entrySet())
            if (event.getDamage(entry.getKey()) != entry.getValue())
                return false;

        return event.getDamage() == this.damage
                && event.getCause() == this.cause;
    }
}
