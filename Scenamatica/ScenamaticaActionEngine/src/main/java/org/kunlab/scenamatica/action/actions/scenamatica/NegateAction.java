package org.kunlab.scenamatica.action.actions.scenamatica;

import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.bookkeeper.annotations.InputDoc;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;

// 特別：NegateActionは、 Scenamatica ネイティブなため, ほとんどの処理は Engine や CompilerSupportSupport で行われる。（密結合）
@Action(NegateAction.ACTION_NAME)
@ActionDoc(
        name = "アクションの否定",
        description = "コンディション要求アクションの結果を否定します。",

        executable = ActionDoc.UNALLOWED,
        expectable = ActionDoc.UNALLOWED,
        requireable = "条件が否定されていることを要求します。"
)
public class NegateAction extends AbstractScenamaticaAction
        implements Requireable
{
    public static final String ACTION_NAME = "negate";

    public static final String KEY_IN_ACTION = "action";
    public static final String KEY_IN_ARGUMENTS = "with";

    @InputDoc(
            name = "action",
            description = "否定するアクションです。",
            type = Object.class
    )
    public static final InputToken<Requireable> IN_ACTION = ofInput(KEY_IN_ACTION, Requireable.class);

    @InputDoc(
            name = "with",
            description = "アクションの引数です。",
            type = Object.class
    )
    public static final InputToken<ActionContext> IN_ARGUMENTS = ofInput(KEY_IN_ARGUMENTS, ActionContext.class);

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        return ofInputs(
                type,
                IN_ACTION,
                IN_ARGUMENTS
        );
    }

    @Override
    public boolean checkConditionFulfilled(@NotNull ActionContext ctxt)
    {
        Requireable requireable = ctxt.input(IN_ACTION);
        return !requireable.checkConditionFulfilled(ctxt.input(IN_ARGUMENTS));
    }
}
