package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")  // DamageModifier <- very soon で消えるらしい。 1.8 の頃から言ってる。 <- 石油かよ！！？
public class EntityDamageAction extends AbstractGeneralEntityAction
        implements Executable, Watchable
{
    public static final String KEY_ACTION_NAME = "entity_damage";
    public static final InputToken<EntityDamageEvent.DamageCause> IN_CAUSE = ofEnumInput(
            "cause",
            EntityDamageEvent.DamageCause.class
    );
    public static final InputToken<Double> IN_AMOUNT = ofInput(
            "amount",
            Double.class
    );
    public static final InputToken<Map<EntityDamageEvent.DamageModifier, Double>> IN_MODIFIERS = ofInput(
            "modifiers",
            InputTypeToken.ofMap(EntityDamageEvent.DamageModifier.class, Double.class),
            ofTraverser(Map.class, (ser, map) -> {
                Map<EntityDamageEvent.DamageModifier, Double> modifiersMap = new HashMap<>();
                Map<String, Number> modifiersMapRaw = MapUtils.checkAndCastMap(
                        map, String.class, Number.class
                );

                for (Map.Entry<String, Number> entry : modifiersMapRaw.entrySet())
                {
                    EntityDamageEvent.DamageModifier cause = EntityDamageEvent.DamageModifier.valueOf(entry.getKey());
                    modifiersMap.put(cause, entry.getValue().doubleValue());
                }

                return modifiersMap;
            })
    );

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Entity target = this.selectTarget(ctxt);

        if (!(target instanceof Damageable))
            throw new IllegalArgumentException("Target is not damageable");

        ((Damageable) target).damage(ctxt.input(IN_AMOUNT));
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedEntityEvent(ctxt, event))
            return false;

        assert event instanceof EntityDamageEvent;
        EntityDamageEvent e = (EntityDamageEvent) event;

        if (ctxt.hasInput(IN_MODIFIERS))
            for (Map.Entry<EntityDamageEvent.DamageModifier, Double> entry : ctxt.input(IN_MODIFIERS).entrySet())
                if (e.getDamage(entry.getKey()) != entry.getValue())
                    return false;

        return ctxt.ifHasInput(IN_CAUSE, cause -> cause == e.getCause())
                && ctxt.ifHasInput(IN_AMOUNT, amount -> amount == e.getDamage());
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityDamageEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_CAUSE, IN_AMOUNT, IN_MODIFIERS);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_AMOUNT)
                    .validator(in -> !in.isPresent(IN_CAUSE), "Use entity_damage_by_entity or entity_damage_by_block action instead");

        return board;
    }
}
