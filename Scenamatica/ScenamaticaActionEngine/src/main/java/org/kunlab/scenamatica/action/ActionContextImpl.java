package org.kunlab.scenamatica.action;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ActionResultCause;
import org.kunlab.scenamatica.enums.RunAs;
import org.kunlab.scenamatica.enums.RunOn;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.LoadedAction;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputReference;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.input.InputValueHolder;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

@Slf4j
@Getter
public class ActionContextImpl implements ActionContext
{
    private static final String KEY_SEPARATOR = ".".replace(".", "\\.");

    private final ScenarioType type;
    private final ScenarioEngine engine;
    private final InputBoard input;
    private final Map<String, Object> output;
    private final Logger logger;
    private final UUID contextID;
    private final StructureSerializer serializer;
    private final RunOn runOn;
    private final RunAs runAs;

    private boolean doOutput;
    @Setter
    private String scenarioName;
    private Boolean success;
    private ActionResultCause cause;
    private boolean halt;
    private boolean skipped;
    private Throwable error;

    public ActionContextImpl(@NotNull ScenarioType type, @NotNull ScenarioEngine engine, @NotNull RunOn runOn, @NotNull RunAs runAs, @NotNull InputBoard inputBoard, @NotNull Logger logger)
    {
        this.type = type;
        this.engine = engine;
        this.runOn = runOn;
        this.runAs = runAs;
        this.input = inputBoard;
        this.logger = logger;
        this.serializer = engine.getManager().getRegistry().getScenarioFileManager().getSerializer();

        this.contextID = UUID.randomUUID();

        this.output = new HashMap<>();
        this.doOutput = true;
    }

    public Context getContext()
    {
        return this.engine.getContext();
    }

    @Override
    public void success()
    {
        this.success = true;
    }

    private void success(ActionResultCause cause)
    {
        this.success = true;
        this.cause = cause;
    }

    @Override
    public void halt()
    {
        this.halt = true;
    }

    @Override
    public void noHalt()
    {
        this.halt = false;
    }

    @Override

    public void skip()
    {
        this.cause = ActionResultCause.SKIPPED;
        this.skipped = true;
    }

    @Override
    public void fail()
    {
        this.success = false;
        this.halt();
    }

    @Override
    public void fail(@NotNull ActionResultCause cause)
    {
        this.cause = cause;
        this.fail();
    }

    @Override
    public void fail(@NotNull Throwable err)
    {
        this.fail();
    }

    @Override
    public void fail(@NotNull ActionResultCause cause, @Nullable Throwable err)
    {
        this.fail();
        this.cause = cause;
        this.error = err;
    }

    @Override
    public @Nullable Throwable getError()
    {
        return this.error;
    }

    @Override
    public boolean isSuccess()
    {
        return this.success == null || this.success;
    }

    @Override
    public void output(String key, Object value)
    {
        if (!this.doOutput)
            return;

        String[] keys = key.split(KEY_SEPARATOR);
        if (keys.length == 0)
            throw new IllegalArgumentException("key is empty");

        Map<String, Object> map = this.output;
        for (int i = 0; i < keys.length - 1; i++)
        {
            String k = keys[i];
            if (!map.containsKey(k))
                map.put(k, new HashMap<String, Object>());
            // noinspection unchecked
            map = (Map<String, Object>) map.get(k);
        }

        map.put(keys[keys.length - 1], value);
    }

    @Override
    public void outputs(Object... kvPairs)
    {
        if (!this.doOutput)
            return;
        else if (kvPairs.length % 2 != 0)
            throw new IllegalArgumentException("kvPairs.length % 2 != 0");

        for (int i = 0; i < kvPairs.length; i += 2)
            this.output(kvPairs[i].toString(), kvPairs[i + 1]);
    }

    @Override
    public void commitOutput()
    {
        if (!this.doOutput)
            return;
        this.engine.getExecutor().uploadScenarioOutputs(this, this.output);
        this.output.clear();
    }

    @Override
    public boolean hasSuccess()
    {
        return this.success != null;
    }

    @Override
    public <T> T input(InputToken<T> token)
    {
        return this.input.get(token);
    }

    @Override
    public <T> boolean hasInput(InputToken<T> token)
    {
        return this.input.isPresent(token);
    }

    @Override
    public <T> T orElseInput(InputToken<? extends T> token, @NotNull Supplier<? extends T> defaultValue)
    {
        return this.input.orElse(token, defaultValue);
    }

    @Override
    public <T> boolean runIfHasInput(@NotNull InputToken<T> token, @NotNull Consumer<? super T> consumer)
    {
        return this.input.runIfPresent(token, consumer);
    }

    @Override
    public <T, U> U ifHasInput(@NotNull InputToken<T> token, @NotNull Function<? super T, ? extends U> mapper, @Nullable U defaultValue)
    {
        return this.input.ifPresent(token, mapper, defaultValue);
    }

    @Override
    public <T> boolean ifHasInput(@NotNull InputToken<T> token, @NotNull Predicate<? super T> predicate, boolean defaultValue)
    {
        return this.input.ifPresent(token, predicate, defaultValue);
    }

    @Override
    public <T> boolean ifHasInput(@NotNull InputToken<T> token, @NotNull Predicate<? super T> predicate)
    {
        return this.input.ifPresent(token, predicate);
    }

    @Override
    public <T> InputValueHolder<T> getHolder(InputToken<T> token)
    {
        return this.input.getHolder(token);
    }

    @Override
    public Optional<Actor> getActor(@NotNull Player bukkitEntity)
    {
        ActorManager actorManager = this.engine.getManager().getRegistry().getContextManager().getActorManager();
        if (actorManager.isActor(bukkitEntity))
            return Optional.ofNullable(actorManager.getByUUID(bukkitEntity.getUniqueId()));
        else
            return Optional.empty();
    }

    @Override
    @NotNull
    public Actor getActorOrThrow(@NotNull Player bukkitEntity)
    {
        return this.getActor(bukkitEntity)
                .orElseThrow(() -> new IllegalArgumentException("Only actor is allowed in this action: Invalid target: " + bukkitEntity.getName()));
    }

    @Override
    public <T extends Action> T findAction(Class<T> actionClass)
    {
        LoadedAction<T> action = this.engine.getManager().getRegistry().getActionManager().getLoader().getActionByClass(actionClass);
        return action == null ? null: action.getInstance();
    }

    @Override
    public ActionContext renew(InputBoard input)
    {
        return new ActionContextImpl(this.type, this.engine, this.getRunOn(), this.getRunAs(), input, this.logger);
    }

    @Override
    public ActionResult createResult(@NotNull CompiledAction action)
    {
        return ActionResultImpl.fromContext(action.getExecutor(), this);
    }

    @Override
    public void doOutput(boolean doOutput)
    {
        this.doOutput = doOutput;
    }

    @Override
    public boolean doOutput()
    {
        return this.doOutput;
    }

    /**
     * リセットします。
     */
    @Override
    public void reset()
    {
        this.success = null;
        this.cause = null;
        this.halt = false;
        this.skipped = false;
        this.error = null;
        this.output.clear();
    }

    @Override
    public String[] getUnresolvedReferences()
    {
        List<InputToken<?>> unresolvedTokens = this.input.getUnresolvedTokens();

        return unresolvedTokens.stream()
                // Token => InputReferences 化
                .map(this.input::getHolder)
                .map(InputValueHolder::getValueReference)
                // InputReferences => 参照文字列の取得
                .map(InputReference::getContainingReferences)
                .filter(Objects::nonNull)
                .flatMap(Arrays::stream)
                .distinct()
                .toArray(String[]::new);
    }
}
