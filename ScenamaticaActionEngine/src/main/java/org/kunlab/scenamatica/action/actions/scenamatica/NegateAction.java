package org.kunlab.scenamatica.action.actions.scenamatica;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.actions.AbstractActionArgument;
import org.kunlab.scenamatica.interfaces.action.ActionArgument;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Map;
import java.util.Objects;

// 特別：NegateActionは、 Scenamatica ネイティブなため, ほとんどの処理は Engine や InternalCompiler で行われる。（密結合）
public class NegateAction<T extends ActionArgument> extends AbstractScenamaticaAction<NegateAction.Argument<T>>
        implements Requireable<NegateAction.Argument<T>>
{
    // ここを変える場合は, ScenarioEngine の ActionCompiler もかえること。
    public static final String KEY_ACTION_NAME = "negate";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public Argument<T> deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument<T> argument, @NotNull ScenarioEngine engine)
    {
        assert argument != null;
        // noinspection rawtypes  呼び出しの型に齟齬が起きる。
        Requireable requireable = argument.getAction();

        // noinspection unchecked  呼び出しの型に齟齬が起きる。
        return !requireable.isConditionFulfilled(argument.getArgument(), engine);
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument<A extends ActionArgument> extends AbstractActionArgument
    {
        public static final String KEY_ACTION = "action";
        public static final String KEY_ARGUMENTS = "with";

        @NotNull
        Requireable<A> action;
        @Nullable
        A argument;

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            try
            {
                // noinspection unchecked
                Argument<A> arg = (Argument<A>) argument;

                return Objects.equals(this.action, arg.action);
            }
            catch (ClassCastException e)
            {
                return false;
            }
        }

        @Override
        public String getArgumentString()
        {
            return buildArgumentString(
                    KEY_ACTION, this.action.getClass().getSimpleName(),
                    KEY_ARGUMENTS, this.argument == null ? null: this.argument.getArgumentString()
            );
        }
    }
}
