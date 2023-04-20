package net.kunmc.lab.scenamatica.scenario;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.kunmc.lab.scenamatica.enums.MilestoneScope;
import net.kunmc.lab.scenamatica.events.MilestoneReachedEvent;
import net.kunmc.lab.scenamatica.events.MilestoneRevokedEvent;
import net.kunmc.lab.scenamatica.interfaces.scenario.MilestoneEntry;
import net.kunmc.lab.scenamatica.interfaces.scenario.MilestoneManager;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MilestoneManagerImpl implements MilestoneManager
{
    private final Multimap<ScenarioEngine, MilestoneEntry> milestones;

    public MilestoneManagerImpl()
    {
        this.milestones = ArrayListMultimap.create();
    }

    @Override
    public boolean reachMilestone(@NotNull ScenarioEngine engine, @NotNull String name)
    {
        return this.reachMilestone(engine, name, MilestoneScope.SCENARIO_GLOBAL);
    }

    @Override
    public boolean reachMilestone(@NotNull ScenarioEngine engine, @NotNull String name, @NotNull MilestoneScope scope)
    {
        return this.reachMilestone(this.getOrInitMilestone(engine, name, scope));
    }

    @Override
    public boolean reachMilestone(@NotNull MilestoneEntry entry)
    {
        if (entry.isReached())
            return false;  // すでに到達済みなので何もしない

        MilestoneReachedEvent event = new MilestoneReachedEvent(entry);
        Bukkit.getServer().getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;

        assert entry instanceof MilestoneEntryImpl;
        ((MilestoneEntryImpl) entry).setReached$1(true);

        return true;
    }

    @Override
    public boolean isReached(@NotNull ScenarioEngine engine, @NotNull String name)
    {
        MilestoneEntry entry = this.getMilestone(engine, name);
        return entry != null && entry.isReached();
    }

    @Override
    public void revokeMilestone(@NotNull MilestoneEntry entry)
    {
        MilestoneRevokedEvent event = new MilestoneRevokedEvent(entry);
        Bukkit.getServer().getPluginManager().callEvent(event);

        assert entry instanceof MilestoneEntryImpl;
        ((MilestoneEntryImpl) entry).setReached$1(false);

        this.milestones.remove(entry.getEngine(), entry);
    }

    @Override
    public void revokeMilestone(@NotNull ScenarioEngine engine, @NotNull String name)
    {
        MilestoneEntry entry = this.getMilestone(engine, name);
        if (entry == null)
            return;

        this.revokeMilestone(entry);
    }

    @Override
    public void revokeAllMilestones(@NotNull ScenarioEngine engine)
    {
        List<MilestoneEntry> entries = new ArrayList<>(this.milestones.get(engine));  // ConcurrentModificationException 回避
        for (MilestoneEntry entry : entries)
            this.revokeMilestone(engine, entry.getName());

        // キーは自動的に削除される
    }

    @Override
    public void revokeAllMilestones(@NotNull ScenarioEngine engine, @NotNull MilestoneScope scope)
    {
        List<MilestoneEntry> entries = new ArrayList<>(this.milestones.get(engine));  // ConcurrentModificationException 回避
        for (MilestoneEntry entry : entries)
            if (entry.getScope() == scope)
                this.revokeMilestone(entry);
    }

    @Override
    public void revokeAllMilestones(@NotNull Plugin plugin)
    {
        List<ScenarioEngine> engines = new ArrayList<>(this.milestones.keySet());  // ConcurrentModificationException 回避
        for (ScenarioEngine engine : engines)
            if (engine.getPlugin().getName().equals(plugin.getName()))
                this.revokeAllMilestones(engine);
    }

    @Override
    @Nullable
    public MilestoneEntry getMilestone(@NotNull ScenarioEngine engine, @NotNull String name)
    {
        return this.milestones.get(engine).stream()
                .filter(e -> e.getName().equalsIgnoreCase(name))
                .findFirst()
                .orElse(null);
    }

    @NotNull
    private MilestoneEntry getOrInitMilestone(ScenarioEngine engine, String name, MilestoneScope scope)
    {
        MilestoneEntry entry = this.getMilestone(engine, name);
        if (entry == null)
        {
            // マイルストーンが存在しないので初期化する
            entry = new MilestoneEntryImpl(
                    this,
                    name,
                    engine.getPlugin(),
                    engine,
                    scope
            );
            this.milestones.put(engine, entry);
        }

        return entry;
    }


}
