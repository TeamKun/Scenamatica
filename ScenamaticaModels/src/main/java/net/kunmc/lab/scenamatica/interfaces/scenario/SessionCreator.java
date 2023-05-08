package net.kunmc.lab.scenamatica.interfaces.scenario;

import lombok.AllArgsConstructor;
import lombok.Value;
import net.kunmc.lab.scenamatica.enums.TriggerType;
import net.kunmc.lab.scenamatica.exceptions.scenario.ScenarioNotFoundException;
import net.kunmc.lab.scenamatica.exceptions.scenario.TriggerNotFoundException;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;

public interface SessionCreator
{
    SessionCreator add(@NotNull Plugin plugin, @NotNull TriggerType trigger, @NotNull String name);

    SessionCreator add(@NotNull ScenarioEngine engine, @NotNull TriggerType trigger);

    List<SessionElement> getSessions();

    void queueAll() throws TriggerNotFoundException, ScenarioNotFoundException;

    boolean isEmpty();

    @Value
    @AllArgsConstructor
    class SessionElement
    {
        Plugin plugin;
        String name;
        // or
        ScenarioEngine engine;

        @NotNull
        TriggerType type;
        @Nullable
        Consumer<? super ScenarioResult> callback;

        public SessionElement(Plugin plugin, String name, @NotNull TriggerType type)
        {
            this(plugin, name, type, null);
        }

        public SessionElement(ScenarioEngine engine, @NotNull TriggerType type)
        {
            this(null, null, engine, type, null);
        }

        public SessionElement(Plugin plugin, String name, @NotNull TriggerType type, @Nullable Consumer<? super ScenarioResult> callback)
        {
            this(plugin, name, null, type, callback);
        }

        public SessionElement(ScenarioEngine engine, @NotNull TriggerType type, @Nullable Consumer<? super ScenarioResult> callback)
        {
            this(null, null, engine, type, callback);
        }
    }
}
