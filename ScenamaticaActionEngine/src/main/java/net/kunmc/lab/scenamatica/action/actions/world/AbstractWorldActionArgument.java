package net.kunmc.lab.scenamatica.action.actions.world;

import lombok.*;
import net.kunmc.lab.scenamatica.commons.utils.*;
import net.kunmc.lab.scenamatica.interfaces.action.*;
import net.kunmc.lab.scenamatica.interfaces.scenario.*;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.*;
import org.apache.commons.lang.*;
import org.bukkit.*;
import org.jetbrains.annotations.*;

import java.util.*;

@AllArgsConstructor
public abstract class AbstractWorldActionArgument implements ActionArgument
{
    private static final String[] PADDING_TARGET = {"the_end", "nether"};

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

    public World getWorld()
    {
        NamespacedKey key = this.worldRef;
        World world;
        if ((world = Bukkit.getWorld(key)) != null)
            return world;

        if (ArrayUtils.contains(PADDING_TARGET, key.getKey()))
            return Bukkit.getWorld(NamespaceUtils.fromString(key.getNamespace() + ":" + "world_" + key.getKey()));

        return null;
    }

    public World getWorldNonNull(ScenarioEngine engine)
    {
        if (this.worldRef == null)
            return engine.getManager().getRegistry().getContextManager().getStageManager().getStage();

        return getWorld();
    }

    @Override
    public String getArgumentString()
    {
        return "world=" + this.worldRef;
    }
}
