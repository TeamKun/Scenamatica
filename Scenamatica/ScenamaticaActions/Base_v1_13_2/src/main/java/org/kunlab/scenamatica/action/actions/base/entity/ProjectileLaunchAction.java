package org.kunlab.scenamatica.action.actions.base.entity;

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
import org.kunlab.scenamatica.annotations.action.Action;
import org.kunlab.scenamatica.bookkeeper.annotations.ActionDoc;
import org.kunlab.scenamatica.commons.utils.EntityUtils;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.exceptions.scenario.IllegalActionInputException;
import org.kunlab.scenamatica.exceptions.scenario.IllegalScenarioStateException;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Expectable;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.EntityStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.entity.entities.ProjectileStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.BlockStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.ProjectileSourceStructure;
import org.kunlab.scenamatica.interfaces.structures.minecraft.misc.SelectorProjectileSourceStructure;
import org.kunlab.scenamatica.selector.Selector;

import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.List;

@Action("projectile_launch")
@ActionDoc(
        name = "投射物の発射",
        description = "投射物を発射します。",
        events = {
                ProjectileLaunchEvent.class
        },

        executable = "投射物を発射します。",
        expectable = "投射物が発射されることを期待します。",
        requireable = ActionDoc.UNALLOWED
)
public class ProjectileLaunchAction extends EntitySpawnAction<Projectile>
        implements Executable, Expectable, Listener
{
    private final Plugin plugin;

    public ProjectileLaunchAction()
    {
        super(Projectile.class, ProjectileStructure.class);

        this.plugin = getPlugin();
    }

    private static Plugin getPlugin()
    {
        ClassLoader classLoader = MethodHandles.lookup().lookupClass().getClassLoader();
        if (!(classLoader instanceof PluginClassLoader))
            throw new IllegalScenarioStateException("ClassLoader is not PluginClassLoader");

        PluginClassLoader pluginClassLoader = (PluginClassLoader) classLoader;
        Plugin plugin = pluginClassLoader.getPlugin();
        if (plugin == null)
            throw new IllegalScenarioStateException("Can't specify your plugin.");

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
                throw new IllegalScenarioStateException("Block must be ProjectileSource");
        }
    }

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        ProjectileStructure entity = (ProjectileStructure) ctxt.input(this.IN_ENTITY).getTargetStructure();
        // noinspection unchecked -> Checked by validator
        Class<? extends Projectile> entityType = (Class<? extends Projectile>) entity.getType().getEntityClass();
        assert entityType != null;

        ProjectileSourceStructure shooter = entity.getShooter();
        ProjectileSource source = this.convertProjectileSource(shooter, ctxt);

        RegisteredListener transformer = this.registerTransformer(ctxt, entity.getType(), entity);
        Vector velocity = entity.getVelocity();
        if (velocity == null)
            source.launchProjectile(entityType);
        else
            source.launchProjectile(entityType, velocity);

        this.unregisterTransformer(transformer);
    }

    private RegisteredListener registerTransformer(ActionContext ctxt, EntityType type, ProjectileStructure structure)
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
                super.makeOutputs(ctxt, e.getEntity());
            }
        };

        RegisteredListener registeredListener = new RegisteredListener(
                dummyListener,
                executor,
                EventPriority.HIGHEST,
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

    private ProjectileSource convertProjectileSource(ProjectileSourceStructure structure, ActionContext ctxt)
    {
        if (structure instanceof SelectorProjectileSourceStructure)
        {
            String selector = ((SelectorProjectileSourceStructure) structure).getSelectorString();
            return Selector.compile(selector)
                    .select().stream()
                    .filter(e -> e instanceof ProjectileSource)
                    .map(e -> (ProjectileSource) e)
                    .findFirst()
                    .orElseThrow(() -> new IllegalActionInputException("Selector must select ProjectileSource"));
        }
        else if (structure instanceof EntityStructure)
        {
            EntityStructure entity = (EntityStructure) structure;
            return (ProjectileSource) EntityUtils.getEntity(entity, ctxt.getEngine(), e -> e instanceof ProjectileSource);
        }
        else if (structure instanceof BlockStructure)
        {
            BlockStructure blockStructure = (BlockStructure) structure;
            if (blockStructure.getLocation() == null)
                throw new IllegalScenarioStateException("BlockStructure must have location");
            Block block = Utils.assignWorldToBlockLocation(blockStructure, ctxt.getEngine()).getBlock();
            return getBlockProjectileSource(block);
        }
        else
            throw new IllegalActionInputException("Invalid ProjectileSourceStructure: " + structure);
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
