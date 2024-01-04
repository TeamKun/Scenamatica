package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EntityDamageByEntityAction extends EntityDamageAction
        implements Executable
{
    public static final String KEY_ACTION_NAME = "entity_damage_by_entity";
    public static final InputToken<EntitySpecifier<Entity>> IN_DAMAGER =  // 殴った人
            ofSpecifier("damager");
    public static final String OUT_KEY_DAMAGER = "damager";

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

        Entity damager = ctxt.input(IN_DAMAGER).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalStateException("Cannot select damager for this action, please specify damager with valid specifier."));

        this.makeOutputs(ctxt, target, damager, null, ctxt.input(IN_AMOUNT), null);
        ((Damageable) target).damage(ctxt.input(IN_AMOUNT), damager);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof EntityDamageByEntityEvent || super.checkFired(ctxt, event)))
            return false;

        assert event instanceof EntityDamageByEntityEvent;
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

        boolean result = ctxt.ifHasInput(IN_DAMAGER, damager -> damager.checkMatchedEntity(e.getDamager()));
        if (result)
            this.makeOutputs(ctxt, e.getEntity(), e.getDamager(), e.getCause(), e.getDamage(), createModifiersMap(e));

        return result;
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull Entity entity, @NotNull Entity damager, EntityDamageEvent.DamageCause cause, double amount, @Nullable Map<String, Double> modifiers)
    {
        ctxt.output(OUT_KEY_DAMAGER, damager);
        super.makeOutputs(ctxt, entity, cause, amount, modifiers);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                EntityDamageByEntityEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_DAMAGER);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_DAMAGER);

        return board;
    }
}
