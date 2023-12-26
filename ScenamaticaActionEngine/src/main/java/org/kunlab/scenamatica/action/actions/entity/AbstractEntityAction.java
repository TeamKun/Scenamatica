package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractEntityAction<E extends Entity> extends AbstractAction
{
    public static final String KEY_TARGET_ENTITY = "target";

    public final InputToken<EntitySpecifier<E>> IN_TARGET_ENTITY;

    public AbstractEntityAction(Class<E> entityClass, Class<? extends EntityStructure> structureClazz)
    {
        this.IN_TARGET_ENTITY = ofInput(KEY_TARGET_ENTITY, entityClass, structureClazz);
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

    protected boolean checkMatchedEntityEvent(@NotNull InputBoard argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!(event instanceof EntityEvent))
            return false;

        EntityEvent e = (EntityEvent) event;
        return argument.ifPresent(this.IN_TARGET_ENTITY, specifier -> specifier.checkMatchedEntity(e.getEntity()));
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

    public E selectTarget(@NotNull InputBoard board, @Nullable Context context)
    {
        return board.get(this.IN_TARGET_ENTITY).selectTarget(context)
                .orElseThrow(() -> new IllegalStateException("Cannot select target for this action, please specify target with valid specifier."));
    }

    public E selectTarget(@NotNull InputBoard board, @NotNull ScenarioEngine engine)
    {
        return this.selectTarget(board, engine.getContext());
    }
}
