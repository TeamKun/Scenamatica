package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.commons.utils.EntityUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
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

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        Entity target = this.selectTarget(argument, engine);
        EntityStructure entityInfo = argument.get(IN_ENTITY);

        if (!(entityInfo instanceof Mapped<?>))
            throw new IllegalStateException("Cannot check matched entity for non-mapped entity");

        // noinspection rawtypes
        Mapped mapped = (Mapped) entityInfo;

        if (!mapped.canApplyTo(target))
            throw new IllegalStateException("Cannot apply entity info of " + entityInfo + " to " + target);


        // noinspection unchecked  // checked above
        mapped.applyTo(target);
    }

    @Override
    public boolean isConditionFulfilled(@NotNull InputBoard argument, @NotNull ScenarioEngine engine)
    {
        Entity target = this.selectTarget(argument, engine);

        if (target == null && !argument.isPresent(IN_ENTITY))
            throw new IllegalStateException("Cannot find entity");
        else if (target == null)
            return false;

        return argument.ifPresent(IN_ENTITY, entity -> EntityUtils.tryCastMapped(entity, target).isAdequate(target));
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .requirePresent(IN_ENTITY);

        if (type == ScenarioType.ACTION_EXECUTE)
            board.requirePresent(IN_ENTITY);

        return board;
    }
}
