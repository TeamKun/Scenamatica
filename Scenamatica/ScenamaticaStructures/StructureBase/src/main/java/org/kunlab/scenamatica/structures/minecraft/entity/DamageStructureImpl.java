package org.kunlab.scenamatica.structures.minecraft.entity;

import lombok.Value;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.DamageStructure;

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

    public static boolean validate(@NotNull Map<String, Object> map)
    {
        MapUtils.checkEnumNameIfContains(map, KEY_CAUSE, EntityDamageEvent.DamageCause.class);
        MapUtils.checkType(map, KEY_DAMAGE, Number.class);
        if (map.containsKey(KEY_DAMAGE))
        {
            Map<String, Object> modifiers = MapUtils.checkAndCastMap(map);
            for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values())
                MapUtils.checkNumberIfContains(modifiers, modifier.name().toLowerCase());
        }

        return true;
    }

    @NotNull
    public static DamageStructure deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);
        Map<EntityDamageEvent.DamageModifier, Double> modifiers = new EnumMap<>(EntityDamageEvent.DamageModifier.class);
        if (map.containsKey(KEY_MODIFIER))
            for (Map.Entry<String, Object> entry : MapUtils.checkAndCastMap(map.get(KEY_MODIFIER)).entrySet())
                modifiers.put(EntityDamageEvent.DamageModifier.valueOf(entry.getKey().toUpperCase()), Double.parseDouble(entry.getValue().toString()));

        return new DamageStructureImpl(
                modifiers,
                MapUtils.getAsEnumOrDefault(map, KEY_CAUSE, EntityDamageEvent.DamageCause.class, EntityDamageEvent.DamageCause.CUSTOM),
                Double.parseDouble(map.get(KEY_DAMAGE).toString())
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
    public void applyTo(EntityDamageEvent object)
    {
        for (Map.Entry<EntityDamageEvent.DamageModifier, Double> entry : this.modifiers.entrySet())
            object.setDamage(entry.getKey(), entry.getValue());
        object.setDamage(this.damage);
    }

    @Override
    public boolean isAdequate(EntityDamageEvent object, boolean ignored)
    {
        for (Map.Entry<EntityDamageEvent.DamageModifier, Double> entry : this.modifiers.entrySet())
            if (object.getDamage(entry.getKey()) != entry.getValue())
                return false;

        return object.getDamage() == this.damage
                && object.getCause() == this.cause;
    }
}
