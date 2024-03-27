package org.kunlab.scenamatica.context.stage;

import lombok.Data;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.enums.StageType;
import org.kunlab.scenamatica.exceptions.context.stage.StageAlreadyDestroyedException;
import org.kunlab.scenamatica.interfaces.context.Stage;
import org.kunlab.scenamatica.interfaces.context.StageManager;

import java.lang.ref.WeakReference;
import java.util.Objects;

@Data
public class StageImpl implements Stage
{
    private final WeakReference<World> world;
    private final StageType type;
    private final StageManager manager;

    private boolean destroyed;

    public StageImpl(World world, StageType type, StageManager manager)
    {
        this.world = new WeakReference<>(world);
        this.type = type;
        this.manager = manager;
    }

    @Override
    public @NotNull World getWorld()
    {
        World world = this.world.get();
        if (world == null)
            throw new IllegalStateException(new StageAlreadyDestroyedException("Stage already destroyed"));

        return world;
    }

    @Override
    public void destroy() throws StageAlreadyDestroyedException
    {
        if (this.destroyed)
            throw new StageAlreadyDestroyedException("Stage already destroyed");

        this.destroyed = true;

        this.manager.destroyStage(this);
    }

    @Override
    public boolean equals(Object object)
    {
        if (this == object) return true;
        if (!(object instanceof StageImpl)) return false;
        StageImpl stage = (StageImpl) object;
        return this.isDestroyed() == stage.isDestroyed()
                && Objects.equals(this.getWorld(), stage.getWorld())
                && this.getType() == stage.getType()
                && Objects.equals(this.getManager(), stage.getManager());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.getWorld(), this.getType(), this.getManager(), this.isDestroyed());
    }
}
