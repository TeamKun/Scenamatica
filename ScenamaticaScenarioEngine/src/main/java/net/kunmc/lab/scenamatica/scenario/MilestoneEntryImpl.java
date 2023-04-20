package net.kunmc.lab.scenamatica.scenario;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import net.kunmc.lab.scenamatica.enums.MilestoneScope;
import net.kunmc.lab.scenamatica.interfaces.scenario.MilestoneEntry;
import net.kunmc.lab.scenamatica.interfaces.scenario.MilestoneManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

@Data
public class MilestoneEntryImpl implements MilestoneEntry
{
    @Getter(AccessLevel.NONE)
    private final MilestoneManager manager;

    @NotNull
    private final String name;
    @NotNull
    private final Plugin plugin;
    @NotNull
    private final ScenarioEngine engine;
    @NotNull
    private MilestoneScope scope;
    private volatile boolean reached;

    @Override
    public boolean isReached()
    {
        return this.reached;
    }

    @Override
    public void setReached(boolean reached)
    {
        if (this.reached == reached)
            return;

        this.manager.reachMilestone(this);
    }

    /* non-public */
    synchronized void setReached$1(boolean reached)
    {
        this.reached = reached;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (!(o instanceof MilestoneEntryImpl)) return false;
        MilestoneEntryImpl that = (MilestoneEntryImpl) o;
        return this.reached == that.reached &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.plugin.getName(), that.plugin.getName());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.name, this.plugin.getName(), this.reached);
    }
}
