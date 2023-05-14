package net.kunmc.lab.scenamatica.action.actions.world;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

@AllArgsConstructor
public abstract class AbstractWorldActionArgument implements ActionArgument
{
    public static final String KEY_WORLD = "world";

    @Nullable
    @Getter
    private final NamespacedKey worldRef;

    @Override
    public boolean isSame(TriggerArgument argument)
    {
        if (!(argument instanceof ActionArgument))
            return false;

        if (!AbstractWorldActionArgument.class.isAssignableFrom(argument.getClass()))
            return false;

        AbstractWorldActionArgument a = (AbstractWorldActionArgument) argument;

        return this.isSameWorld(a);
    }

    protected boolean isSameWorld(AbstractWorldActionArgument argument)
    {
        return Objects.equals(this.worldRef, argument.worldRef);
    }

    public World getWorld(ScenarioEngine engine)
    {
        if (this.worldRef == null)
            return engine.getManager().getRegistry().getContextManager().getStageManager().getStage();
        else
            return Bukkit.getWorld(this.worldRef);
    }

    @Override
    public String getArgumentString()
    {
        return "world=" + this.worldRef;
    }
}
