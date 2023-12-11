package org.kunlab.scenamatica.action.actions.entity;

import lombok.EqualsAndHashCode;
import lombok.Value;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
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
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.kunlab.scenamatica.action.utils.EventListenerUtils;
import org.kunlab.scenamatica.commons.specifiers.EntitySpecifierImpl;
import org.kunlab.scenamatica.commons.utils.EntityUtils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenario.ScenarioEngine;
import org.kunlab.scenamatica.interfaces.scenariofile.StructureSerializer;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.ProjectileSourceStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.SelectorProjectileSourceStructure;
import org.kunlab.scenamatica.interfaces.scenariofile.specifiers.EntitySpecifier;
import org.kunlab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProjectileLaunchAction extends EntitySpawnAction<ProjectileLaunchAction.Argument>
        implements Executable<ProjectileLaunchAction.Argument>, Watchable<ProjectileLaunchAction.Argument>, Listener
{
    public static final String KEY_ACTION_NAME = "entity_project_launch";

    private final Plugin plugin;

    public ProjectileLaunchAction()
    {
        super();
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

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        ProjectileStructure entity = (ProjectileStructure) argument.getEntity().getTargetStructure();
        // noinspection unchecked -> Checked by validator
        Class<? extends Projectile> entityType = (Class<? extends Projectile>) entity.getClass();

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
            return EntityUtils.selectEntities(selector)
                    .stream()
                    .filter(e -> e instanceof ProjectileSource)
                    .map(e -> (ProjectileSource) e)
                    .findFirst()
                    .orElse(null);
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
            Block block = blockStructure.getLocation().getBlock();
            if (block.getState() instanceof ProjectileSource)
                return (ProjectileSource) block.getState();
            else
                throw new IllegalArgumentException("Block must be ProjectileSource");
        }
        else
            throw new IllegalArgumentException("Invalid ProjectileSourceStructure: " + structure);
    }

    @Override
    public boolean isFired(@NotNull Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        assert event instanceof ProjectileLaunchEvent;
        ProjectileLaunchEvent e = (ProjectileLaunchEvent) event;

        EntitySpecifier<?> entity = argument.getEntity();
        if (!entity.checkMatchedEntity(e.getEntity()))
            return false;

        if (argument.getEntity() != null)
        {
            if (argument.getEntity().isSelectable() && !argument.getEntity().checkMatchedEntity(e.getEntity()))
                return false;
            else if (argument.getEntity().hasStructure())
            {
                ProjectileStructure structure = (ProjectileStructure) argument.getEntity().getTargetStructure();
                return structure.isAdequate(e.getEntity());
            }
        }

        return true;
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                ProjectileLaunchEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull StructureSerializer serializer)
    {
        return new Argument(
                EntitySpecifierImpl.tryDeserialize(
                        map.get(EntitySpawnAction.Argument.KEY_ENTITY),
                        serializer,
                        ProjectileStructure.class
                )
        );
    }

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends EntitySpawnAction.Argument
    {
        public Argument(@NotNull EntitySpecifier<? extends Projectile> projectile)
        {
            super(projectile);
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof Argument))
                return false;

            Argument arg = (Argument) argument;
            return super.isSame(arg);
        }

        @Override
        public void validate(@NotNull ScenarioEngine engine, @NotNull ScenarioType type)
        {
            super.validate(engine, type);
            if (type == ScenarioType.ACTION_EXECUTE)
            {
                ProjectileStructure structure = (ProjectileStructure) this.getEntity().getTargetStructure();
                assert structure.getType().getEntityClass() != null;
                if (structure.getType().getEntityClass().isAssignableFrom(Projectile.class))
                    throw new IllegalArgumentException("ProjectileStructure must have projectile type");

                ProjectileSourceStructure shooter = structure.getShooter();
                if (shooter == null)
                    throw new IllegalArgumentException("ProjectileStructure must have shooter");
            }
        }

        @Override
        public EntitySpecifier<? extends Projectile> getEntity()
        {
            // noinspection unchecked -> Checked by superclass
            return (EntitySpecifier<? extends Projectile>) super.getEntity();
        }
    }
}
