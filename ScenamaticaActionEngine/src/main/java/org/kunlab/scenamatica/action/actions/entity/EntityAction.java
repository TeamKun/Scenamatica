package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.EntityUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.scenariofile.Mapped;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;

public class EntityAction extends AbstractGeneralEntityAction
        implements Executable, Requireable
{
    public static final String KEY_ACTION_NAME = "entity";

    public static final InputToken<EntityStructure> IN_ENTITY = ofInput(
            "entity",
            EntityStructure.class,
            ofDeserializer(EntityStructure.class)
    );

    public static final String OUT_KEY_ENTITY = "entity";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Entity target = this.selectTarget(ctxt);
        EntityStructure entityInfo = ctxt.input(IN_ENTITY);

        if (!(entityInfo instanceof Mapped<?>))
            throw new IllegalStateException("Cannot check matched entity for non-mapped entity");

        // noinspection rawtypes
        Mapped mapped = (Mapped) entityInfo;

        if (!mapped.canApplyTo(target))
            throw new IllegalStateException("Cannot apply entity info of " + entityInfo + " to " + target);

        this.makeOutputs(ctxt, target);

        // noinspection unchecked  // checked above
        mapped.applyTo(target);
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Entity target = this.selectTarget(ctxt);

        if (target == null && !ctxt.hasInput(IN_ENTITY))
            throw new IllegalStateException("Cannot find entity");
        else if (target == null)
            return false;

        boolean result = ctxt.ifHasInput(IN_ENTITY, entity -> EntityUtils.tryCastMapped(entity, target).isAdequate(target));
        if (result)
            this.makeOutputs(ctxt, target);

        return result;
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .register(IN_ENTITY);

        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_ENTITY);

        return board;
    }
}
