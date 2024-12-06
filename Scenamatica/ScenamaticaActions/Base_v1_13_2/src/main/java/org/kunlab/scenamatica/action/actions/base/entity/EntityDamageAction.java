package org.kunlab.scenamatica.action.actions.base.entity;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.InputTypeToken;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings("deprecation")  // DamageModifier <- very soon で消えるらしい。 1.8 の頃から言ってる。 <- 石油かよ！！？
@Action("entity_damage")
@ActionDoc(
        name = "エンティティの負傷",
        description = "エンティティにダメージを与えます。",
        events = {
                EntityDamageEvent.class
        },

        executable = "エンティティにダメージを与えます。",
        expectable = "エンティティがダメージを受けることを期待します。",
        requireable = ActionDoc.UNALLOWED,

        outputs = {
                @OutputDoc(
                        name = EntityDamageAction.KEY_OUT_CAUSE,
                        description = "ダメージの原因です。",
                        type = EntityDamageEvent.DamageCause.class
                ),
                @OutputDoc(
                        name = EntityDamageAction.KEY_OUT_AMOUNT,
                        description = "ダメージの量です。",
                        type = double.class
                ),
                @OutputDoc(
                        name = EntityDamageAction.KEY_OUT_MODIFIERS,
                        description = "ダメージの修飾子です。",
                        type = Map.class
                )
        }

)
public class EntityDamageAction extends AbstractGeneralEntityAction
        implements Executable, Expectable
{
    @InputDoc(
            name = "cause",
            description = "ダメージの原因を指定します。",
            type = EntityDamageEvent.DamageCause.class
    )
    public static final InputToken<EntityDamageEvent.DamageCause> IN_CAUSE = ofEnumInput(
            "cause",
            EntityDamageEvent.DamageCause.class
    );

    @InputDoc(
            name = "amount",
            description = "ダメージの量を指定します。\n"
                    + "いわゆる `EntityDamageEvent#getFinalDamage()` に相当します。",
            type = double.class,
            min = 0.0d,
            requiredOn = ActionMethod.EXECUTE
    )
    public static final InputToken<Double> IN_AMOUNT = ofInput(
            "amount",
            Double.class
    );

    @InputDoc(
            name = "modifiers",
            description = "ダメージの修飾子を指定します。",
            type = Map.class
    )
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

    public static final String KEY_OUT_CAUSE = "cause";
    public static final String KEY_OUT_AMOUNT = "amount";
    public static final String KEY_OUT_MODIFIERS = "modifiers";

    protected static Map<String, Double> createModifiersMap(EntityDamageEvent evt)
    {
        Map<String, Double> modifiersMap = new HashMap<>();
        for (EntityDamageEvent.DamageModifier modifier : EntityDamageEvent.DamageModifier.values())
            modifiersMap.put(modifier.name().toLowerCase(), evt.getDamage(modifier));

        return modifiersMap;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Entity target = this.selectTarget(ctxt);

        if (!(target instanceof Damageable))
            throw new IllegalActionInputException("Target is not damageable");

        this.makeOutputs(ctxt, target, null, ctxt.input(IN_AMOUNT), null);
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

        boolean result = ctxt.ifHasInput(IN_CAUSE, cause -> cause == e.getCause())
                && ctxt.ifHasInput(IN_AMOUNT, amount -> amount == e.getDamage());
        if (result)
            this.makeOutputs(ctxt, e.getEntity(), e.getCause(), e.getDamage(), createModifiersMap(e));

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Entity entity, @Nullable EntityDamageEvent.DamageCause cause, double amount, @Nullable Map<String, Double> modifiers)
    {
        if (cause != null)
            ctxt.output(KEY_OUT_CAUSE, cause);
        ctxt.output(KEY_OUT_AMOUNT, amount);
        if (modifiers != null)
            ctxt.output(KEY_OUT_MODIFIERS, modifiers);
        super.makeOutputs(ctxt, entity);
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
