package org.kunlab.scenamatica.action.actions.base.player;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SmallFireball;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.ThrownExpBottle;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.Trident;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.Event;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.kunlab.scenamatica.annotations.action.ActionMeta;
import org.kunlab.scenamatica.commons.utils.Utils;
import org.kunlab.scenamatica.enums.MinecraftVersion;
import org.kunlab.scenamatica.enums.ScenarioType;
import org.kunlab.scenamatica.interfaces.action.ActionContext;
import org.kunlab.scenamatica.interfaces.action.input.InputBoard;
import org.kunlab.scenamatica.interfaces.action.input.InputToken;
import org.kunlab.scenamatica.interfaces.action.types.Executable;
import org.kunlab.scenamatica.interfaces.action.types.Watchable;
import org.kunlab.scenamatica.interfaces.scenariofile.misc.LocationStructure;

import java.util.Collections;
import java.util.List;

@ActionMeta(value = "player_projectile_launch", supportsUntil = MinecraftVersion.V1_15_2)
public class PlayerLaunchProjectileAction extends AbstractPlayerAction
        implements Executable, Watchable
{
    public static final InputToken<ProjectileType> IN_PROJECTILE_TYPE = ofEnumInput(
            "projectileType",
            ProjectileType.class
    );
    public static final InputToken<LocationStructure> IN_VELOCITY = ofInput(
            "velocity",
            LocationStructure.class,
            ofDeserializer(LocationStructure.class)
    );
    public static final InputToken<Double> IN_EPSILON = ofInput(
            "epsilon",
            Double.class,
            0.01
    );
    public static final String KEY_OUT_PROJECTILE = "projectile";

    @Override
    public void execute(@NotNull ActionContext ctxt)
    {
        Player player = selectTarget(ctxt);
        ProjectileType type = ctxt.input(IN_PROJECTILE_TYPE);
        Vector velocity = ctxt.input(IN_VELOCITY).create().toVector();

        this.makeOutputs(ctxt, player);
        Projectile proj = player.launchProjectile(type.getClazz(), velocity);
        this.makeLazyOutputs(ctxt, player, proj);
    }

    @Override
    public boolean checkFired(@NotNull ActionContext ctxt, @NotNull Event event)
    {
        if (!super.checkMatchedPlayerEvent(ctxt, event))
            return false;

        assert event instanceof PlayerLaunchProjectileEvent;
        PlayerLaunchProjectileEvent e = (PlayerLaunchProjectileEvent) event;

        ProjectileType projectileType = ProjectileType.fromProjectile(e.getProjectile());
        Vector velocity = e.getProjectile().getVelocity().clone();

        boolean result = ctxt.ifHasInput(IN_PROJECTILE_TYPE, type -> type == projectileType)
                && ctxt.ifHasInput(
                IN_VELOCITY,
                loc -> Utils.vectorEquals(loc.create().toVector(), velocity, ctxt.input(IN_EPSILON))
        );

        if (result)
            this.makeLazyOutputs(ctxt, e.getPlayer(), e.getProjectile());

        return result;
    }

    protected void makeLazyOutputs(@NotNull ActionContext ctxt, @NotNull Player player, @NotNull Projectile projectile)
    {
        ctxt.output(KEY_OUT_PROJECTILE, projectile);
        super.makeOutputs(ctxt, player);
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerLaunchProjectileEvent.class
        );
    }

    @Override
    public InputBoard getInputBoard(ScenarioType type)
    {
        InputBoard board = super.getInputBoard(type)
                .registerAll(IN_PROJECTILE_TYPE, IN_VELOCITY, IN_EPSILON);
        if (type == ScenarioType.CONDITION_REQUIRE)
            board.requirePresent(IN_PROJECTILE_TYPE, IN_VELOCITY);

        return board;
    }

    @AllArgsConstructor
    @Getter
    public enum ProjectileType
    {
        ARROW(Arrow.class),
        SNOWBALL(Snowball.class),
        EGG(Egg.class),
        FIREBALL(Fireball.class),
        SMALL_FIREBALL(SmallFireball.class),
        ENDER_PEARL(EnderPearl.class),
        WITHER_SKULL(WitherSkull.class),
        SHULKER_BULLET(ShulkerBullet.class),
        DRAGON_FIREBALL(DragonFireball.class),
        POTION(ThrownPotion.class),
        EXPERIENCE_BOTTLE(ThrownExpBottle.class),
        ITEM(ThrownPotion.class),
        TRIDENT(Trident.class);

        private final Class<? extends Projectile> clazz;

        public static <T extends Projectile> ProjectileType fromProjectile(T proj)
        {
            return fromClass(proj.getClass());
        }

        public static <T extends Projectile> ProjectileType fromClass(Class<T> clazz)
        {
            for (ProjectileType type : values())
            {
                if (type.getClazz().isAssignableFrom(clazz))
                    return type;
            }
            return null;
        }
    }

}
