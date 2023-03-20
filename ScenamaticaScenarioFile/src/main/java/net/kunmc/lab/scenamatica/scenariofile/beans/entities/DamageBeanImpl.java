package net.kunmc.lab.scenamatica.scenariofile.beans.entities;

import lombok.Value;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.entities.DamageBean;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Value
@SuppressWarnings("deprecation")
public class DamageBeanImpl implements DamageBean
{
    @NotNull
    EntityDamageEvent.DamageModifier modifier;
    @NotNull
    EntityDamageEvent.DamageCause cause;
    double damage;

    public static Map<String, Object> serialize(@NotNull DamageBean bean)
    {
        Map<String, Object> map = new HashMap<>();
        map.put(KEY_DAMAGE, bean.getDamage());
        if (bean.getModifier() != EntityDamageEvent.DamageModifier.BASE)
            map.put(KEY_MODIFIER, bean.getModifier().name());
        if (bean.getCause() != EntityDamageEvent.DamageCause.CUSTOM)
            map.put(KEY_CAUSE, bean.getCause().name());

        return map;
    }

    public static boolean validate(@NotNull Map<String, Object> map)
    {
        MapUtils.checkEnumNameIfContains(map, KEY_MODIFIER, EntityDamageEvent.DamageModifier.class);
        MapUtils.checkEnumNameIfContains(map, KEY_CAUSE, EntityDamageEvent.DamageCause.class);
        MapUtils.checkType(map, KEY_DAMAGE, Number.class);

        return true;
    }

    public static DamageBean deserialize(@NotNull Map<String, Object> map)
    {
        validate(map);
        return new DamageBeanImpl(
                MapUtils.getAsEnumOrDefault(map, KEY_MODIFIER, EntityDamageEvent.DamageModifier.class, EntityDamageEvent.DamageModifier.BASE),
                MapUtils.getAsEnumOrDefault(map, KEY_CAUSE, EntityDamageEvent.DamageCause.class, EntityDamageEvent.DamageCause.CUSTOM),
                Double.parseDouble(map.get(KEY_DAMAGE).toString())
        );
    }
}
