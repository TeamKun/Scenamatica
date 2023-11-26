package org.kunlab.scenamatica.scenariofile.structures.entity;

import lombok.Value;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.DamageStructure;

import java.util.HashMap;
import java.util.Map;

@Value
@SuppressWarnings("deprecation")
public class DamageStructureImpl implements DamageStructure
{
    @NotNull
    EntityDamageEvent.DamageModifier modifier;
    @NotNull
    EntityDamageEvent.DamageCause cause;
    double damage;

    @NotNull
    public static Map<String, Object> serialize(@NotNull DamageStructure structure)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_DAMAGE, structure.getDamage());
        if (structure.getModifier() != EntityDamageEvent.DamageModifier.BASE)
            map.put(KEY_MODIFIER, structure.getModifier().name());
        if (structure.getCause() != EntityDamageEvent.DamageCause.CUSTOM)
            map.put(KEY_CAUSE, structure.getCause().name());

        return map;
    }

    public static boolean validate(@NotNull Map<String, Object> map)
    {
        MapUtils.checkEnumNameIfContains(map, KEY_MODIFIER, EntityDamageEvent.DamageModifier.class);
        MapUtils.checkEnumNameIfContains(map, KEY_CAUSE, EntityDamageEvent.DamageCause.class);
        MapUtils.checkType(map, KEY_DAMAGE, Number.class);

        return true;
    }

    @NotNull
    public static DamageStructure deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);
        return new DamageStructureImpl(
                MapUtils.getAsEnumOrDefault(map, KEY_MODIFIER, EntityDamageEvent.DamageModifier.class, EntityDamageEvent.DamageModifier.BASE),
                MapUtils.getAsEnumOrDefault(map, KEY_CAUSE, EntityDamageEvent.DamageCause.class, EntityDamageEvent.DamageCause.CUSTOM),
                Double.parseDouble(map.get(KEY_DAMAGE).toString())
        );
    }
}
