package org.kunlab.scenamatica.action.actions.entity;

import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.block.Dispenser;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Projectile;
import org.bukkit.event.Event;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.plugin.EventExecutor;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.PluginClassLoader;
import org.bukkit.projectiles.BlockProjectileSource;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.action.utils.EventListenerUtils;
import org.kunlab.scenamatica.commons.utils.EntityUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.ProjectileSourceStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.SelectorProjectileSourceStructure;
import org.kunlab.scenamatica.selector.Selector;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

public class ProjectileLaunchAction extends EntitySpawnAction<Projectile>
        implements Executable, Watchable, Listener
{
    public static final String KEY_ACTION_NAME = "projectile_launch";

    private final Plugin plugin;

    public ProjectileLaunchAction()
    {
        super(Projectile.class, ProjectileStructure.class);

        this.plugin = getPlugin();

        Bukkit.getPluginManager().registerEvents(this, this.plugin);

    }

    private static Plugin getPlugin()
    {
        ClassLoader classLoader = MethodHandles.lookup().lookupClass().getClassLoader();
        if (!(classLoader instanceof PluginClassLoader))
            throw new IllegalArgumentException("ClassLoader is not PluginClassLoader");

        PluginClassLoader pluginClassLoader = (PluginClassLoader) classLoader;
        Plugin plugin = pluginClassLoader.getPlugin();
        if (plugin == null)
            throw new IllegalStateException("Can't specify your plugin.");

        return plugin;
    }

    private static BlockProjectileSource getBlockProjectileSource(Block block)
    {
        switch (block.getType())
        {
            case DISPENSER:
                // noinspection deprecation
            case LEGACY_DISPENSER:
                return ((Dispenser) block.getState()).getBlockProjectileSource();
            default:
                throw new IllegalArgumentException("Block must be ProjectileSource");
        }
    }

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @NotNull InputBoard argument)
    {
        ProjectileStructure entity = (ProjectileStructure) argument.get(this.IN_ENTITY).getTargetStructure();
        // noinspection unchecked -> Checked by validator
        Class<? extends Projectile> entityType = (Class<? extends Projectile>) entity.getType().getEntityClass();
        assert entityType != null;

        ProjectileSourceStructure shooter = entity.getShooter();
        ProjectileSource source = this.convertProjectileSource(shooter, engine);

        RegisteredListener transformer = this.registerTransformer(entity.getType(), entity);
        Vector velocity = entity.getVelocity();
        if (velocity == null)
            source.launchProjectile(entityType);
        else
            source.launchProjectile(entityType, velocity);

        this.unregisterTransformer(transformer);
    }

    private RegisteredListener registerTransformer(EntityType type, ProjectileStructure structure)
    {
        Listener dummyListener = new Listener()
        {
        };

        EventExecutor executor = (listener1, event) -> {
            if (event instanceof ProjectileLaunchEvent)
            {
                ProjectileLaunchEvent e = (ProjectileLaunchEvent) event;
                if (e.getEntityType() == type)
                    structure.applyTo(e.getEntity());
            }
        };

        RegisteredListener registeredListener = new RegisteredListener(
                dummyListener,
                executor,
                EventPriority.MONITOR,  // ほかのイベントハンドラによる改変を許可。
                this.plugin,
                false
        );

        EventListenerUtils.getListeners(ProjectileLaunchEvent.class).register(registeredListener);

        return registeredListener;
    }

    private void unregisterTransformer(RegisteredListener listener)
    {
        EventListenerUtils.getListeners(ProjectileLaunchEvent.class).unregister(listener);
    }

    private ProjectileSource convertProjectileSource(ProjectileSourceStructure structure, ScenarioEngine engine)
    {
        if (structure instanceof SelectorProjectileSourceStructure)
        {
            String selector = ((SelectorProjectileSourceStructure) structure).getSelectorString();
            return Selector.compile(selector)
                    .select().stream()
                    .filter(e -> e instanceof ProjectileSource)
                    .map(e -> (ProjectileSource) e)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Selector must select ProjectileSource"));
        }
        else if (structure instanceof EntityStructure)
        {
            EntityStructure entity = (EntityStructure) structure;
            return (ProjectileSource) EntityUtils.getEntity(entity, engine, e -> e instanceof ProjectileSource);
        }
        else if (structure instanceof BlockStructure)
        {
            BlockStructure blockStructure = (BlockStructure) structure;
            if (blockStructure.getLocation() == null)
                throw new IllegalArgumentException("BlockStructure must have location");
            Block block = Utils.assignWorldToBlockLocation(blockStructure, engine).getBlock();
            return getBlockProjectileSource(block);
        }
        else
            throw new IllegalArgumentException("Invalid ProjectileSourceStructure: " + structure);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                ProjectileLaunchEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type);
        if (type == ScenarioType.ACTION_EXECUTE)
            board.validator(b -> {
                        EntityStructure structure = b.get(this.IN_ENTITY).getTargetStructure();
                        assert structure != null;
                        return ProjectileStructure.class.isAssignableFrom(structure.getClass());
                    }, "EntityStructure must be ProjectileStructure")
                    .validator(b -> {
                                ProjectileStructure structure = (ProjectileStructure) b.get(this.IN_ENTITY).getTargetStructure();
                                assert structure != null;
                                return structure.getShooter() != null;
                            }, "ProjectileStructure must have shooter"
                    );

        return board;
    }
}
