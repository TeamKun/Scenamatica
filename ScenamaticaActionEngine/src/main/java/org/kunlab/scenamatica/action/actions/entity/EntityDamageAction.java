package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@SuppressWarnings("deprecation")  // DamageModifier <- very soon で消えるらしい。 1.8 の頃から言ってる。 <- 石油かよ！！？
public class EntityDamageAction<A extends EntityDamageAction.Argument> extends AbstractEntityAction<A>
        implements Executable<A>, Watchable<A>
{
    public static final String KEY_ACTION_NAME = "entity_damage";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable A argument)
    {
        argument = this.requireArgsNonNull(argument);

        Entity target = argument.selectTarget(engine.getContext());

        if (!(target instanceof Damageable))
            throw new IllegalArgumentException("Target is not damageable");

        ((Damageable) target).damage(argument.getAmount());
    }

    @Override
    public boolean isFired(@NotNull A argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(argument, engine, event))
            return false;

        assert event instanceof EntityDamageEvent;
        EntityDamageEvent e = (EntityDamageEvent) event;

        if (argument.getModifiers() != null)
            for (Map.Entry<EntityDamageEvent.DamageModifier, Double> entry : argument.getModifiers().entrySet())
                if (e.getDamage(entry.getKey()) != entry.getValue())
                    return false;

        return (argument.getCause() == null || argument.getCause() == e.getCause())
                && (argument.getAmount() == null || argument.getAmount() == e.getFinalDamage());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityDamageEvent.class
        );
    }

    @Override
    public A deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        Number amountNum = MapUtils.getAsNumberOrNull(map, Argument.KEY_AMOUNT);
        Double amount;
        if (amountNum == null)
            amount = null;
        else
            amount = amountNum.doubleValue();

        Map<EntityDamageEvent.DamageModifier, Double> modifiersMap;
        if (map.containsKey(Argument.KEY_DAMAGE_MODIFIERS))
        {
            modifiersMap = new HashMap<>();

            Map<String, Number> modifiersMapRaw = MapUtils.checkAndCastMap(
                    map, String.class, Number.class
            );

            for (Map.Entry<String, Number> entry : modifiersMapRaw.entrySet())
            {
                EntityDamageEvent.DamageModifier cause = EntityDamageEvent.DamageModifier.valueOf(entry.getKey());
                modifiersMap.put(cause, entry.getValue().doubleValue());
            }
        }
        else
            modifiersMap = null;

        // noinspection unchecked  A は Argument であることが保証されている
        return (A) new Argument(
                super.deserializeTarget(map, serializer),
                MapUtils.getAsEnumOrNull(map, Argument.KEY_DAMAGE_CAUSE, EntityDamageEvent.DamageCause.class),
                amount,
                modifiersMap
        );
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractEntityActionArgument<Entity>
    {
        public static final String KEY_DAMAGE_CAUSE = "cause";
        public static final String KEY_AMOUNT = "amount";  // 最終ダメージ
        public static final String KEY_DAMAGE_MODIFIERS = "modifiers";

        private final EntityDamageEvent.DamageCause cause;
        private final Double amount;
        private final Map<EntityDamageEvent.DamageModifier, @NotNull Double> modifiers;

        public Argument(EntitySpecifier<Entity> target, EntityDamageEvent.DamageCause cause, Double amount, Map<EntityDamageEvent.DamageModifier, @NotNull Double> modifiers)
        {
            super(target);
            this.cause = cause;
            this.amount = amount;
            this.modifiers = modifiers;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return this.cause == arg.cause
                    && Objects.equals(this.amount, arg.amount)
                    && MapUtils.equals(this.modifiers, this.modifiers);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                this.throwIfNotSelectable();
                ensurePresent(Argument.KEY_AMOUNT, this.amount);
                if (this.cause != null)
                    throw new IllegalArgumentException("Use entity_damage_by_entity or entity_damage_by_block action instead.");
            }
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_DAMAGE_CAUSE, this.cause,
                    KEY_AMOUNT, this.amount,
                    KEY_DAMAGE_MODIFIERS, this.modifiers
            );
        }
    }
}
