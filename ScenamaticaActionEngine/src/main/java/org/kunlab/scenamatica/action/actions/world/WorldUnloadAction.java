package org.kunlab.scenamatica.action.actions.world;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.event.Event;
import org.bukkit.event.world.WorldUnloadEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.commons.utils.MapUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Requireable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class WorldUnloadAction extends AbstractWorldAction<WorldUnloadAction.Argument>
        implements Executable<WorldUnloadAction.Argument>, Requireable<WorldUnloadAction.Argument>
{
    public static final String KEY_ACTION_NAME = "world_unload";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull WorldUnloadAction.Argument argument)
    {
        World world = argument.getWorldNonNull(engine);

        Bukkit.getServer().unloadWorld(world, !Boolean.FALSE.equals(argument.getSave()));  // ぬるぽ回避 && デフォは true
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                WorldUnloadEvent.class
        );
    }

    @Override
    public boolean isConditionFulfilled(@NotNull WorldUnloadAction.Argument argument, @NotNull ScenarioEngine engine)
    {
        NamespacedKey key = argument.getWorldRef();
        assert key != null;

        return Bukkit.getWorld(key) == null;
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new Argument(
                super.deserializeWorld(map),
                MapUtils.getOrNull(map, Argument.KEY_SAVE)
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractWorldActionArgument
    {
        public static final String KEY_SAVE = "save";

        @Nullable
        Boolean save;

        public Argument(@Nullable NamespacedKey worldRef, Boolean save)
        {
            super(worldRef);
            this.save = save;
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
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            if (!(type == ScenarioType.ACTION_EXECUTE || this.save == null))
                throw new IllegalArgumentException("Argument 'save' is only available in 'action_execute' type.");
        }

        @Override
        public String getArgumentString()
        {
            return appendArgumentString(
                    super.getArgumentString(),
                    KEY_SAVE, this.save
            );
        }
    }
}
