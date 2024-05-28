package org.kunlab.scenamatica.action.actions.base.entity;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.OutputDocs;
import org.kunlab.scenamatica.commons.utils.EntityUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;

@Action("entity")
@ActionDoc(
        name = "エンティティの情報の変更",
        description = "エンティティの情報を設定します。",

        executable = "エンティティの状態や属性を変更します。",
        watchable = ActionDoc.UNALLOWED,
        requireable = "エンティティの状態や属性が指定されたものと一致するかどうかを確認します。"
)
public class EntityAction extends AbstractGeneralEntityAction
        implements Executable, Requireable
{
    @InputDoc(
            name = "data",
            description = "変更するエンティティのデータを指定します。",
            type = EntityStructure.class
    )
    public static final InputToken<EntityStructure> IN_ENTITY = ofInput(
            "data",
            EntityStructure.class,
            ofDeserializer(EntityStructure.class)
    );

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Entity target = this.selectTarget(ctxt);
        EntityStructure entityInfo = ctxt.input(IN_ENTITY);

        EntityUtils.tryCastMapped(entityInfo, target).applyTo(target);
        this.makeOutputs(ctxt, target);
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Entity target = this.selectTarget(ctxt);

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
