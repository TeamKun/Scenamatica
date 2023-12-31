package org.kunlab.scenamatica.action;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ActionResultCause;
import org.kunlab.scenamatica.interfaces.action.Action;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.ActionResult;
import org.kunlab.scenamatica.interfaces.action.CompiledAction;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.input.InputValueHolder;
import org.kunlab.scenamatica.interfaces.context.Actor;
import org.kunlab.scenamatica.interfaces.context.ActorManager;
import org.kunlab.scenamatica.interfaces.context.Context;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;

@Getter
public class ActionContextImpl implements ActionContext
{
    private static final String KEY_SEPARATOR = ".".replace(".", "\\.");

    private final ScenarioEngine engine;
    private final InputBoard input;
    private final Map<String, Object> output;
    private final Logger logger;
    private final UUID contextID;
    private final StructureSerializer serializer;

    @Setter
    private String scenarioName;
    private WeakReference<Context> context;
    private Boolean success;
    private ActionResultCause cause;
    private boolean halt;
    private boolean skipped;
    private Throwable err;

    public ActionContextImpl(@NotNull ScenarioEngine engine, @NotNull InputBoard inputBoard, @NotNull Logger logger)
    {
        this.engine = engine;
        this.input = inputBoard;
        this.logger = logger;
        this.serializer = engine.getManager().getRegistry().getScenarioFileManager().getSerializer();

        this.contextID = UUID.randomUUID();

        this.output = new HashMap<>();
    }

    public Context getContext()
    {
        if (this.context == null || this.context.get() == null)
            this.context = new WeakReference<>(this.engine.getContext());

        return this.context.get();
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
        this.skipped = true;
    }

    @Override
    public void fail()
    {
        this.success = false;
        this.halt();
    }

    @Override
    public void fail(ActionResultCause cause)
    {
        this.fail();
        this.cause = cause;
    }

    @Override
    public void fail(@NotNull Throwable err)
    {
        this.fail();
        this.err = err;
    }

    @Override
    public @Nullable Throwable getError()
    {
        return this.err;
    }

    @Override
    public boolean isSuccess()
    {
        return this.success == null || this.success;
    }

    @Override
    public void output(String key, Object value)
    {
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
        if (kvPairs.length % 2 != 0)
            throw new IllegalArgumentException("kvPairs.length % 2 != 0");

        for (int i = 0; i < kvPairs.length; i += 2)
            this.output(kvPairs[i].toString(), kvPairs[i + 1]);
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
        return this.engine.getManager().getRegistry().getActionManager().getCompiler().findAction(actionClass);
    }

    @Override
    public ActionContext renew(InputBoard input)
    {
        return new ActionContextImpl(this.engine, input, this.logger);
    }

    @Override
    public ActionResult createResult(@NotNull CompiledAction action)
    {
        return ActionResultImpl.fromContext(action.getExecutor(), this);
    }


}
