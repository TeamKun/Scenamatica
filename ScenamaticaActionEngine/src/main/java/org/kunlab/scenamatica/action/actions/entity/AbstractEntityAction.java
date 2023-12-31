package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEntityAction<E extends Entity> extends AbstractAction
{
    public static final String OUT_KEY_ENTITY = "entity";
    public final InputToken<EntitySpecifier<E>> IN_TARGET_ENTITY;

    public AbstractEntityAction(Class<E> entityClass, Class<? extends EntityStructure> structureClazz)
    {
        this.IN_TARGET_ENTITY = ofInput("target", entityClass, structureClazz);
    }

    public AbstractEntityAction()
    {
        this.IN_TARGET_ENTITY = null;
    }

    public static List<? extends AbstractAction> getActions()
    {
        List<AbstractAction> actions = new ArrayList<>();

        actions.add(new EntityAction());
        actions.add(new EntityDamageAction());
        actions.add(new EntityDamageByEntityAction());
        actions.add(new EntityDeathAction());
        actions.add(new EntityDropItemAction());
        actions.add(new EntityMoveAction());
        actions.add(new EntityPickupItemAction());
        actions.add(new EntityPlaceAction());
        actions.add(new EntitySpawnAction<>(Entity.class, EntityStructure.class));  // AbstractEntityAction を継承してない(引数都合)
        actions.add(new ProjectileHitAction());
        actions.add(new ProjectileLaunchAction());

        return actions;
    }

    protected boolean checkMatchedEntityEvent(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof EntityEvent))
            return false;

        EntityEvent e = (EntityEvent) event;
        return ctxt.ifHasInput(this.IN_TARGET_ENTITY, specifier -> specifier.checkMatchedEntity(e.getEntity()));
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @NotNull E entity)
    {
        ctxt.output(OUT_KEY_ENTITY, entity);
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = ofInputs(
                type,
                this.IN_TARGET_ENTITY
        );
        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(this.IN_TARGET_ENTITY);

        return board;
    }

    public E selectTarget(@NotNull ActionContext ctxt)
    {
        return ctxt.input(this.IN_TARGET_ENTITY).selectTarget(ctxt.getContext())
                .orElseThrow(() -> new IllegalStateException("Cannot select target for this action, please specify target with valid specifier."));
    }
}
