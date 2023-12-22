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
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
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

        Entity target = argument.selectTarget(engine.getContext());

        if (!(target instanceof Damageable))
            throw new IllegalArgumentException("Target is not damageable");

        Entity damager = argument.getDamager().selectTarget(engine.getContext())
                .orElseThrow(() -> new IllegalStateException("Cannot select damager for this action, please specify damager with valid specifier."));

        ((Damageable) target).damage(argument.getAmount(), damager);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        argument = this.requireArgsNonNull(argument);
        if (!(event instanceof EntityDamageByEntityEvent || super.isFired(argument, engine, event)))
            return false;

        assert event instanceof EntityDamageByEntityEvent;
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

        return argument.getDamager().checkMatchedEntity(e.getDamager());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityDamageByEntityEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        EntityDamageAction.Argument base = super.deserializeArgument(map, serializer);

        return new Argument(
                base.getTargetHolder(),
                base.getCause(),
                base.getAmount(),
                base.getModifiers(),
                serializer.tryDeserializeEntitySpecifier(map.get(Argument.KEY_DAMAGER))
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends EntityDamageAction.Argument
    {
        public static final String KEY_DAMAGER = "damager";  // 殴った人

        EntitySpecifier<Entity> damager;

        @SuppressWarnings("deprecation")  // DamageModifier は消えるとか言ってるけど多分きえない。たぶん。というかまだある。
        public Argument(EntitySpecifier<Entity> target, EntityDamageEvent.DamageCause cause, Double amount,
                        Map<EntityDamageEvent.DamageModifier, @NotNull Double> modifiers, EntitySpecifier<Entity> damager)
        {
            super(target, cause, amount, modifiers);
            this.damager = damager;
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
                this.ensureCanProvideTarget();
                if (!this.getDamager().canProvideTarget())
                    throw new IllegalArgumentException("Cannot select damager for this action, please specify damager with valid specifier.");
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
