package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.Collections;
import java.util.List;

public class EntityDamageByEntityAction extends EntityDamageAction
        implements Executable
{
    public static final String KEY_ACTION_NAME = "entity_damage_by_entity";
    public static final InputToken<EntitySpecifier<Entity>> IN_DAMAGER =  // 殴った人
            ofSpecifier("damager");

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

        ((Damageable) target).damage(ctxt.input(IN_AMOUNT), damager);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof EntityDamageByEntityEvent || super.checkFired(ctxt, event)))
            return false;

        assert event instanceof EntityDamageByEntityEvent;
        EntityDamageByEntityEvent e = (EntityDamageByEntityEvent) event;

        return ctxt.ifHasInput(IN_DAMAGER, damager -> damager.checkMatchedEntity(e.getDamager()));
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
