package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractAction;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;

public abstract class AbstractEntityAction<E extends Entity, V extends EntityStructure & Mapped<E>> extends AbstractAction
{
    public static final String OUT_KEY_TARGET = "target";
    public final InputToken<EntitySpecifier<E>> IN_TARGET_ENTITY;

    public AbstractEntityAction(Class<E> entityClass, Class<V> structureClazz)
    {
        this.IN_TARGET_ENTITY = ofInput("target", entityClass, structureClazz);
    }

    protected boolean checkMatchedEntityEvent(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!(event instanceof EntityEvent))
            return false;

        EntityEvent e = (EntityEvent) event;
        return ctxt.ifHasInput(this.IN_TARGET_ENTITY, specifier -> specifier.checkMatchedEntity(e.getEntity()));
    }

    protected void makeOutputs(@NotNull ActionContext ctxt, @Nullable E entity)
    {
        if (entity != null)
            ctxt.output(OUT_KEY_TARGET, entity);
        ctxt.commitOutput();
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
