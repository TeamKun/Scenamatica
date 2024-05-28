package org.kunlab.scenamatica.action.actions.base.entity;

import org.bukkit.entity.Entity;
import org.bukkit.event.Event;
import org.bukkit.event.entity.EntityEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.AbstractAction;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.kunlab.scenamatica.bookkeeper.enums.ActionMethod;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.specifiers.EntitySpecifier;

@OutputDocs({
        @OutputDoc(
                name = AbstractEntityAction.OUT_KEY_TARGET,
                description = "対象となったエンティティです。",
                type = Entity.class
        )
})
public abstract class AbstractEntityAction<E extends Entity, V extends EntityStructure & Mapped<E>> extends AbstractAction
{
    public static final String OUT_KEY_TARGET = "target";
    @InputDoc(
            name = "target",
            description = "対象となるエンティティです。",
            type = EntitySpecifier.class,
            requiredOn = ActionMethod.EXECUTE
    )
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
