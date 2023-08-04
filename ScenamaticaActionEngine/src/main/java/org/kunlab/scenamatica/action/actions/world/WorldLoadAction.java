package org.kunlab.scenamatica.action.actions.world;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.WorldCreator;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldLoadEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.BeanSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WorldLoadAction extends AbstractWorldAction<WorldLoadAction.Argument>
        implements Executable<WorldLoadAction.Argument>, Requireable<WorldLoadAction.Argument>
{
    public static final String KEY_ACTION_NAME = "world_load";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = super.requireArgsNonNull(argument);

        NamespacedKey key = argument.getWorldRef();
        assert key != null;

        Path worldDir = Bukkit.getWorldContainer().toPath();
        if (!Files.exists(worldDir.resolve(key.getKey())))
            if (Files.exists(worldDir.resolve("world_" + key.getKey())))
                key = NamespacedKey.fromString(key.getNamespace() + ":world_" + key.getKey());
            else
                throw new IllegalArgumentException("World '" + key.getKey() + "' does not exist.");
        assert key != null;

        // createWorld は, ワールドが存在する場合は読み込むだけ。
        Bukkit.createWorld(new WorldCreator(key.getKey()));
    }

    @Override
    public boolean isConditionFulfilled(@Nullable Argument argument, @NotNull ScenarioEngine engine)
    {
        argument = super.requireArgsNonNull(argument);
        return argument.getWorld() != null;
    }

    @Override
    public void validateArgument(@NotNull ScenarioEngine engine, @NotNull ScenarioType type, @Nullable Argument argument)
    {
        argument = super.requireArgsNonNull(argument);
        if ((type == ScenarioType.ACTION_EXECUTE || type == ScenarioType.CONDITION_REQUIRE)
                && argument.getWorldRef() == null)
            throw new IllegalArgumentException("Argument 'world' is required in 'action_execute' type.");
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WorldLoadEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        return new Argument(
                super.deserializeWorld(map)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractWorldActionArgument
    {
        public Argument(@Nullable NamespacedKey worldRef)
        {
            super(worldRef);
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;

            return this.isSameWorld(arg);
        }

        @Override
        public String getArgumentString()
        {
            return super.getArgumentString();
        }
    }
}
