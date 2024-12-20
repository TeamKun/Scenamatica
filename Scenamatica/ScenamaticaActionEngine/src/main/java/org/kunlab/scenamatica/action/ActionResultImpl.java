package org.kunlab.scenamatica.action;

import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.ActionMetaUtils;
import org.kunlab.scenamatica.enums.ActionResultCause;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

@Value
public class ActionResultImpl implements ActionResult
{
    @NotNull
    ScenarioType scenarioType;
    @Nullable
    String scenarioName;
    UUID runID;
    boolean success;
    boolean halt;
    @Nullable
    ActionResultCause cause;
    @Nullable
    Throwable error;
    Map<String, Object> outputs;
    @Nullable
    String[] unresolvedReferences;

    public static ActionResult fromContext(Action action, ActionContext context)
    {
        String scenarioName = context.getScenarioName() == null ?
                ActionMetaUtils.getActionName(action):
                context.getScenarioName() + " of action " + ActionMetaUtils.getActionName(action);

        return new ActionResultImpl(
                context.getType(),
                scenarioName,
                context.getContextID(),  // runID は contextID と同じ
                context.isSuccess(),
                context.isHalt(),
                context.getCause(),
                context.getError(),
                Collections.unmodifiableMap(context.getOutput()),
                context.getUnresolvedReferences()
        );
    }

    public static ActionResult fromAction(CompiledAction action)
    {
        return fromContext(action.getExecutor(), action.getContext());
    }

    @Override
    public boolean isSkipped()
    {
        return this.cause == ActionResultCause.SKIPPED;
    }

    @Override
    public boolean isFailed()
    {
        return !this.success;
    }
}
