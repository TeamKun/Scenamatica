package org.kunlab.scenamatica.scenariofile.structures.entity;

import lombok.Value;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.DamageStructure;

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
        for (Map.Entry<EntityDamageEvent.DamageModifier, Double> entry : structure.getModifiers().entrySet())
            map.put(entry.getKey().name().toLowerCase(), entry.getValue());
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
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values())
        {
            if (map.containsKey(modifier.name()))
                modifiers.put(modifier, MapUtils.getAsNumber(map, modifier.name(), Number::doubleValue));
        }

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
}
