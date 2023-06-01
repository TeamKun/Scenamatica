package net.kunmc.lab.scenamatica.action.actions.player;

import com.destroystokyo.paper.event.player.PlayerLaunchProjectileEvent;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Value;
import net.kunmc.lab.scenamatica.action.utils.Utils;
import net.kunmc.lab.scenamatica.commons.utils.MapUtils;
import net.kunmc.lab.scenamatica.interfaces.action.ActionArgument;
import net.kunmc.lab.scenamatica.interfaces.scenario.ScenarioEngine;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.BeanSerializer;
import net.kunmc.lab.scenamatica.interfaces.scenariofile.trigger.TriggerArgument;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
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
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class PlayerLaunchProjectileAction extends AbstractPlayerAction<PlayerLaunchProjectileAction.Argument>
{
    public static final String KEY_ACTION_NAME = "player_projectile_launch";

    @Override
    public String getName()
    {
        return KEY_ACTION_NAME;
    }

    @Override
    public void execute(@NotNull ScenarioEngine engine, @Nullable PlayerLaunchProjectileAction.Argument argument)
    {
        argument = this.requireArgsNonNull(argument);

        Player player = argument.getTarget();

        ProjectileType type = argument.getProjectileType();

        player.launchProjectile(type.getClazz(), argument.getVelocity());
    }

    @Override
    public boolean isFired(@NotNull PlayerLaunchProjectileAction.Argument argument, @NotNull ScenarioEngine engine, @NotNull Event event)
    {
        if (!super.isFired(argument, engine, event))
            return false;

        assert event instanceof PlayerLaunchProjectileEvent;

        PlayerLaunchProjectileEvent e = (PlayerLaunchProjectileEvent) event;

        ProjectileType projectileType = ProjectileType.fromProjectile(e.getProjectile());

        Vector velocity = e.getProjectile().getVelocity().clone().normalize();

        // TODO: Check shooter item
        return /* BeanUtils.isSame(argument.getShooterItem(), e.getItemStack(), false)
                &&*/ argument.getProjectileType() == projectileType
                && (argument.getVelocity() == null || Utils.vectorEquals(argument.getVelocity(), velocity, argument.getEpsilon()));
    }

    @Override
    public List<Class<? extends Event>> getAttachingEvents()
    {
        return Collections.singletonList(
                PlayerLaunchProjectileEvent.class
        );
    }

    @Override
    public Argument deserializeArgument(@NotNull Map<String, Object> map, @NotNull BeanSerializer serializer)
    {
        MapUtils.checkEnumName(map, Argument.KEY_PROJECTILE_TYPE, ProjectileType.class);
        MapUtils.checkTypeIfContains(map, Argument.KEY_PROJECTILE_EPSILON, Double.class);
        // ItemStackBeanImpl.checkMap(map, PlayerLaunchProjectileActionArgument.KEY_PROJECTILE_SHOOTER);

        Map<String, Object> velocityMap = MapUtils.checkAndCastMap(map.get(Argument.KEY_PROJECTILE_VELOCITY),
                String.class, Object.class
        );

        double epsilon;
        if (map.containsKey(Argument.KEY_PROJECTILE_EPSILON) && map.get(Argument.KEY_PROJECTILE_EPSILON) != null)
            epsilon = (double) map.get(Argument.KEY_PROJECTILE_EPSILON);
        else
            epsilon = Argument.DEFAULT_EPSILON;

        return new Argument(
                super.deserializeTarget(map),
                MapUtils.getAsEnum(map, Argument.KEY_PROJECTILE_TYPE, ProjectileType.class),
                /* ItemStackBeanImpl.deserialize(map, PlayerLaunchProjectileActionArgument.KEY_PROJECTILE_SHOOTER) */
                Vector.deserialize(velocityMap),
                epsilon
        );
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
        FIREWORK(Firework.class),
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

    @Value
    @EqualsAndHashCode(callSuper = true)
    public static class Argument extends AbstractPlayerActionArgument
    {
        public static final String KEY_PROJECTILE_TYPE = "type";
        public static final String KEY_PROJECTILE_VELOCITY = "velocity";
        public static final String KEY_PROJECTILE_EPSILON = "epsilon";  // 比較のしきい値。
        // public static final String KEY_PROJECTILE_SHOOTER = "shooterItem";

        public static final double DEFAULT_EPSILON = 0.01;

        // @NotNull
        // ItemStackBean shooterItem;
        @NotNull
        ProjectileType projectileType;
        @Nullable
        Vector velocity;

        double epsilon;

        public Argument(@NotNull String target, @NotNull ProjectileType projectileType)
        {
            super(target);
            this.projectileType = projectileType;
            this.velocity = null;
            this.epsilon = DEFAULT_EPSILON;
        }

        public Argument(@NotNull String target, @NotNull ProjectileType projectileType, @Nullable Vector velocity)
        {
            super(target);
            this.projectileType = projectileType;
            this.velocity = velocity;
            this.epsilon = DEFAULT_EPSILON;
        }

        public Argument(@NotNull String target, @NotNull ProjectileType projectileType, @Nullable Vector velocity, double epsilon)
        {
            super(target);
            this.projectileType = projectileType;
            this.velocity = velocity;
            this.epsilon = epsilon;
        }

        @Override
        public boolean isSame(TriggerArgument argument)
        {
            if (!(argument instanceof ActionArgument))
                return false;

            if (!Argument.class.isAssignableFrom(argument.getClass()))
                return false;

            Argument a = (Argument) argument;

            return this.projectileType == a.projectileType /* && this.shooterItem.isSame(a.shooterItem) */;
        }

        @Override
        public String getArgumentString()
        {
            return String.format(
                    "type=%s, velocity=%s, epsilon=%s",
                    this.projectileType.name(),
                    this.velocity == null ? "null": this.velocity.toString(),
                    this.epsilon
            );
        }
    }
}
