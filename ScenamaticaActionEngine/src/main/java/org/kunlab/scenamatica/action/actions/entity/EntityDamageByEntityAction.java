package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.EntityUtils;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EntityDamageByEntityAction extends EntityDamageAction<EntityDamageByEntityAction.Argument>
        implements Executable<EntityDamageByEntityAction.Argument>
{
    public static final String KEY_ACTION_NAME = "entity_damage_by_entity";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Entity target = argument.selectTarget();

        if (!(target instanceof Damageable))
            throw new IllegalArgumentException("Target is not damageable");

        ((Damageable) target).damage(argument.getAmount(), argument.getDamager());
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        argument = this.requireArgsNonNull(argument);
        if (!(event instanceof EntityDamageByEntityEvent || super.isFired(argument, engine, event)))
            return false;

        assert event instanceof EntityDamageByEntityEvent;
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

        return super.checkMatchedEntity(argument.getDamagerString(), e.getDamager());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityDamageByEntityEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        EntityDamageAction.Argument base = super.deserializeArgument(map, serializer);
        String damager = MapUtils.getOrNull(map, Argument.KEY_DAMAGER);

        return new Argument(
                base.getTargetRaw(),
                base.getCause(),
                base.getAmount(),
                base.getModifiers(),
                damager
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends EntityDamageAction.Argument
    {
        public static final String KEY_DAMAGER = "damager";  // 殴った人

        String damager;

        @SuppressWarnings("deprecation")  // DamageModifier は消えるとか言ってるけど多分きえない。たぶん。というかまだある。
        public Argument(Object target, EntityDamageEvent.DamageCause cause, Double amount, Map<EntityDamageEvent.DamageModifier, @NotNull Double> modifiers, String damager)
        {
            super(target, cause, amount, modifiers);
            this.damager = damager;
        }

        public Entity getDamager()
        {
            return EntityUtils.getPlayerOrEntityOrThrow(this.damager);
        }

        public String getDamagerString()
        {
            return this.damager;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;
            else if (!super.isSame(argument))
                return false;

            Argument arg = (Argument) argument;

            return Objects.equals(this.damager, arg.damager);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);

            if (type == ScenarioType.ACTION_EXECUTE)
            {
                this.throwIfNotSelectable();
                throwIfNotPresent(KEY_DAMAGER, this.damager);
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_DAMAGER, this.damager
            );
        }
    }
}
